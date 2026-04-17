#!/usr/bin/env python3

import sys
import subprocess
import threading
import argparse
import os

# --- Configuration ---
LOG_FILE = os.path.join(os.path.dirname(os.path.realpath(__file__)), "mcp_io.log")
# --- End Configuration ---

# --- Argument Parsing ---
parser = argparse.ArgumentParser(
    description="Wrap a command, passing STDIN/STDOUT while logging them.",
    usage="%(prog)s <command> [args...]"
)
# Capture the command and all subsequent arguments
parser.add_argument('command', nargs=argparse.REMAINDER,
                    help='The command and its arguments to execute.')

open(LOG_FILE, 'w', encoding='utf-8')

if len(sys.argv) == 1:
    parser.print_help(sys.stderr)
    sys.exit(1)

args = parser.parse_args()

if not args.command:
    print("Error: No command provided.", file=sys.stderr)
    parser.print_help(sys.stderr)
    sys.exit(1)

target_command = args.command
# --- End Argument Parsing ---

# --- I/O Forwarding Functions ---
# These will run in separate threads

def forward_and_log_stdin(proxy_stdin, target_stdin, log_file):
    """Reads from proxy's stdin, logs it, writes to target's stdin."""
    try:
        while True:
            # Read line by line from the script's actual stdin
            line_bytes = proxy_stdin.readline()
            if not line_bytes:  # EOF reached
                break

            # Decode for logging (assuming UTF-8, adjust if needed)
            try:
                line_str = line_bytes.decode('utf-8')
            except UnicodeDecodeError:
                line_str = f"[Non-UTF8 data, {len(line_bytes)} bytes]\n" # Log representation

            # Log with prefix
            log_file.write(f"MCP Client发送: {line_str}")
            log_file.flush() # Ensure log is written promptly

            # Write the original bytes to the target process's stdin
            target_stdin.write(line_bytes)
            target_stdin.flush() # Ensure target receives it promptly

    except Exception as e:
        # Log errors happening during forwarding
        try:
            log_file.write(f"!!! STDIN Forwarding Error: {e}\n")
            log_file.flush()
        except: pass # Avoid errors trying to log errors if log file is broken

    finally:
        # Important: Close the target's stdin when proxy's stdin closes
        # This signals EOF to the target process (like test.sh's read loop)
        try:
            target_stdin.close()
            log_file.write("--- STDIN stream closed to target ---\n")
            log_file.flush()
        except Exception as e:
            try:
                log_file.write(f"!!! Error closing target STDIN: {e}\n")
                log_file.flush()
            except: pass


def forward_and_log_stdout(target_stdout, proxy_stdout, log_file):
    """Reads from target's stdout, logs it, writes to proxy's stdout."""
    try:
        while True:
            # Read line by line from the target process's stdout
            line_bytes = target_stdout.readline()
            if not line_bytes: # EOF reached (process exited or closed stdout)
                break

            # Decode for logging
            try:
                line_str = line_bytes.decode('utf-8')
            except UnicodeDecodeError:
                line_str = f"[Non-UTF8 data, {len(line_bytes)} bytes]\n"

            # Log with prefix
            log_file.write(f"MCP Server发送: {line_str}")
            log_file.flush()

            # Write the original bytes to the script's actual stdout
            proxy_stdout.write(line_bytes)
            proxy_stdout.flush() # Ensure output is seen promptly

    except Exception as e:
        try:
            log_file.write(f"!!! STDOUT Forwarding Error: {e}\n")
            log_file.flush()
        except: pass
    finally:
        try:
            log_file.flush()
        except: pass
        # Don't close proxy_stdout (sys.stdout) here

# --- Main Execution ---
process = None
log_f = None
exit_code = 1 # Default exit code in case of early failure

try:
    # Open log file in append mode ('a') for the threads
    log_f = open(LOG_FILE, 'a', encoding='utf-8')

    # Start the target process
    # We use pipes for stdin/stdout
    # We work with bytes (bufsize=0 for unbuffered binary, readline() still works)
    # stderr=subprocess.PIPE could be added to capture stderr too if needed.
    process = subprocess.Popen(
        target_command,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE, # Capture stderr too, good practice
        bufsize=0 # Use 0 for unbuffered binary I/O
    )

    # Pass binary streams to threads
    stdin_thread = threading.Thread(
        target=forward_and_log_stdin,
        args=(sys.stdin.buffer, process.stdin, log_f),
        daemon=True # Allows main thread to exit even if this is stuck (e.g., waiting on stdin) - reconsider if explicit join is needed
    )

    stdout_thread = threading.Thread(
        target=forward_and_log_stdout,
        args=(process.stdout, sys.stdout.buffer, log_f),
        daemon=True
    )

    # Optional: Handle stderr similarly (log and pass through)
    stderr_thread = threading.Thread(
        target=forward_and_log_stdout, # Can reuse the function
        args=(process.stderr, sys.stderr.buffer, log_f), # Pass stderr streams
        # Add a different prefix in the function if needed, or modify function
        # For now, it will log with "STDOUT:" prefix - might want to change function
        # Let's modify the function slightly for this
        daemon=True
    )
    # A slightly modified version for stderr logging
    def forward_and_log_stderr(target_stderr, proxy_stderr, log_file):
        """Reads from target's stderr, logs it, writes to proxy's stderr."""
        try:
            while True:
                line_bytes = target_stderr.readline()
                if not line_bytes: break
                try: line_str = line_bytes.decode('utf-8')
                except UnicodeDecodeError: line_str = f"[Non-UTF8 data, {len(line_bytes)} bytes]\n"
                log_file.write(f"STDERR: {line_str}") # Use STDERR prefix
                log_file.flush()
                proxy_stderr.write(line_bytes)
                proxy_stderr.flush()
        except Exception as e:
            try:
                log_file.write(f"!!! STDERR Forwarding Error: {e}\n")
                log_file.flush()
            except: pass
        finally:
            try:
                log_file.flush()
            except: pass

    stderr_thread = threading.Thread(
        target=forward_and_log_stderr,
        args=(process.stderr, sys.stderr.buffer, log_f),
        daemon=True
    )


    # Start the forwarding threads
    stdin_thread.start()
    stdout_thread.start()
    stderr_thread.start() # Start stderr thread too

    # Wait for the target process to complete
    process.wait()
    exit_code = process.returncode

    # Wait briefly for I/O threads to finish flushing last messages
    # Since they are daemons, they might exit abruptly with the main thread.
    # Joining them ensures cleaner shutdown and logging.
    # We need to make sure the pipes are closed so the reads terminate.
    # process.wait() ensures target process is dead, pipes should close naturally.
    stdin_thread.join(timeout=1.0) # Add timeout in case thread hangs
    stdout_thread.join(timeout=1.0)
    stderr_thread.join(timeout=1.0)


except Exception as e:
    print(f"MCP Logger Error: {e}", file=sys.stderr)
    # Try to log the error too
    if log_f and not log_f.closed:
        try:
            log_f.write(f"!!! MCP Logger Main Error: {e}\n")
            log_f.flush()
        except: pass # Ignore errors during final logging attempt
    exit_code = 1 # Indicate logger failure

finally:
    # Ensure the process is terminated if it's still running (e.g., if logger crashed)
    if process and process.poll() is None:
        try:
            process.terminate()
            process.wait(timeout=1.0) # Give it a moment to terminate
        except: pass # Ignore errors during cleanup
        if process.poll() is None: # Still running?
            try: process.kill() # Force kill
            except: pass # Ignore kill errors

    # Final log message
    if log_f and not log_f.closed:
        try:
            log_f.close()
        except: pass # Ignore errors during final logging attempt

    # Exit with the target process's exit code
    sys.exit(exit_code)