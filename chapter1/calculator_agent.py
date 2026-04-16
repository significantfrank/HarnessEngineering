import os
import json
import re
import httpx
from dotenv import load_dotenv
from openai import OpenAI


# 定义计算工具函数
def multiply(a, b):
    """乘法工具"""
    return float(a) * float(b)


def divide(a, b):
    """除法工具"""
    if float(b) == 0:
        raise ValueError("除数不能为零")
    return float(a) / float(b)


# 解析工具调用
def parse_tool_call(tool_call):
    """解析工具调用指令"""
    pattern = r'<tool>([^<]*)</tool>'
    match = re.search(pattern, tool_call)
    if match:
        return match.group(1).strip()
    return None


# 执行工具调用
def execute_tool(tool_command):
    """执行工具命令并返回结果"""
    try:
        # 解析函数名和参数
        if tool_command.startswith("multiply("):
            # 提取参数
            args_match = re.match(r'multiply\(([^,]+),\s*([^)]+)\)', tool_command)
            if args_match:
                a, b = args_match.groups()
                result = multiply(a, b)
                return f"<tool_output>{result}</tool_output>"

        elif tool_command.startswith("divide("):
            # 提取参数
            args_match = re.match(r'divide\(([^,]+),\s*([^)]+)\)', tool_command)
            if args_match:
                a, b = args_match.groups()
                result = divide(a, b)
                return f"<tool_output>{result}</tool_output>"

        return f"<tool_output>未知工具调用: {tool_command}</tool_output>"

    except Exception as e:
        return f"<tool_output>工具执行错误: {str(e)}</tool_output>"


# 打印发送给AI的请求内容
def print_messages(messages, iteration):
    print("\n" + "=" * 50)
    print(f"第 {iteration} 次API调用, 请求内容：")
    print("=" * 50)

    # 打印整个messages的JSON内容
    print(json.dumps(messages, indent=4, ensure_ascii=False))

    print("=" * 50)

load_dotenv()

# 调用大模型
def invoke_model(user_input):
    # 初始化客户端
    client = OpenAI(
        api_key=os.getenv("DEEPSEEK_API_KEY"),
        base_url="https://api.deepseek.com",
        http_client=httpx.Client(verify=False)
    )

    # 工具使用说明
    tool_use = """      
    当用户问计算相关问题，有必要可以使用工具，每个工具都是函数。
    使用工具的方式为输出 "<tool>使用工具指令</tool>"。
    使用工具后，你要等待回传结果 "<tool_output>工具回传的结果</tool_output>"。
    最后，输出最终结果"<final_result>最终答案</final_result>"

    可用工具：
    multiply(a,b): 返回 a 乘以 b
    divide(a,b): 返回 a 除以 b
    """

    # 初始化
    messages = [
        {"role": "system", "content": tool_use},
        {"role": "user", "content": user_input},
    ]

    max_iterations = 5  # 防止无限循环
    iteration = 0
    while iteration < max_iterations:
        iteration += 1

        print_messages(messages, iteration)

        # 调用API
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=messages,
            stream=False
        )

        assistant_content = response.choices[0].message.content
        print(f"\nAI回复: {assistant_content}")

        # 检查是否需要工具调用
        tool_command = parse_tool_call(assistant_content)

        if tool_command:
            print(f"执行工具: {tool_command}")
            tool_result = execute_tool(tool_command)
            print(f"工具结果: {tool_result}")

            # 将工具结果添加到对话历史
            messages.append({"role": "assistant", "content": assistant_content})
            messages.append({"role": "user", "content": tool_result})

        else:
            # 检查是否有最终结果
            final_match = re.search(r'<final_result>([^<]*)</final_result>', assistant_content)
            if final_match:
                final_answer = final_match.group(1).strip()
                print(f"\n🎉 最终答案: {final_answer}")
            break
    if iteration >= max_iterations:
        print("⚠️  计算过程过于复杂，请简化您的问题。")


# 主函数
def main():
    print("AI计算助手已启动！输入计算问题或输入'退出'结束程序。")

    while True:
        # 获取用户输入
        user_input = input("\n请输入计算问题: ").strip()

        if user_input.lower() in ['退出', 'exit', 'quit']:
            print("再见！")
            break

        if not user_input:
            continue

        invoke_model(user_input)


if __name__ == "__main__":
    main()