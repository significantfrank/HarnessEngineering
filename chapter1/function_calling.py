import os
import json
import httpx
import requests
from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()

client = OpenAI(
    api_key=os.getenv("DEEPSEEK_API_KEY"),
    base_url="https://api.deepseek.com",
    http_client=httpx.Client(verify=False),
)

tools = [
    {
        "type": "function",
        "function": {
            "name": "get_weather",
            "description": "获取指定城市的天气信息",
            "parameters": {
                "type": "object",
                "properties": {
                    "city": {
                        "type": "string",
                        "description": "城市名称，支持中文或英文",
                    }
                },
                "required": ["city"],
            },
        },
    }
]


def get_weather(city):
    url = f"https://wttr.in/{city}?format=3&lang=zh-cn"
    try:
        response = requests.get(url)
        response.raise_for_status()
        return response.text.strip()
    except Exception as e:
        return f"获取天气失败: {e}"


def run(messages):
    response = client.chat.completions.create(
        model="deepseek-chat",
        messages=messages,
        tools=tools,
        tool_choice="auto",
    )

    message = response.choices[0].message

    if message.tool_calls:
        tool_call = message.tool_calls[0]
        fn_name = tool_call.function.name
        fn_args = json.loads(tool_call.function.arguments)

        print(f"模型调用函数: {fn_name}({fn_args})")

        if fn_name == "get_weather":
            result = get_weather(**fn_args)
        else:
            result = f"未知函数: {fn_name}"

        messages.append(message)
        messages.append(
            {
                "role": "tool",
                "tool_call_id": tool_call.id,
                "content": result,
            }
        )

        final_response = client.chat.completions.create(
            model="deepseek-chat",
            messages=messages,
        )
        return final_response.choices[0].message.content

    return message.content


if __name__ == "__main__":
    messages = [
        {"role": "system", "content": "你是一个有用的助手，可以查询天气信息。"},
        {"role": "user", "content": "今天北京天气怎么样？"},
    ]
    answer = run(messages)
    print(answer)
