from  mcp.server.fastmcp import FastMCP
import requests

# 一、创建FastMCP类
mcp = FastMCP("获取天气信息")

# 二、自定义工具（Stdio 模式）
@mcp.tool("get_weather")
def get_weather(city) -> str:
    """
    获取天气信息

     参数:
    city (str): 城市名称

    返回:
    city_weather : 当前城市的天气信息
    """
    url = f"https://wttr.in/{city}?format=3&lang=zh-cn"
    try:
        response = requests.get(url)
        response.raise_for_status()
        return response.text.strip()
    except Exception as e:
        return f"获取天气失败: {e}"

# 三、初始化MCP Server
if __name__ == '__main__':
    # print
    print("MCP Server is running...")
    mcp.run(transport='stdio')
    print("MCP exist")