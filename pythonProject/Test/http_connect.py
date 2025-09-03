import requests
import json
import sys


def validate_token(url:str, token:str):
    """
    向指定URL发送包含JWT Token的请求，验证Token有效性

    Args:
        url (str): 验证接口的URL
        token (str): 要验证的JWT Token

    Returns:
        dict: 服务器返回的响应数据
    """
    # 准备请求数据
    payload = {
        "token": token
    }

    # 设置请求头
    headers = {
        'Content-Type': 'application/json',
        'User-Agent': 'TokenValidator/1.0'
    }

    try:
        # 发送POST请求
        response = requests.post(
            url,
            data=json.dumps(payload),
            headers=headers,
            timeout=10  # 设置超时时间为10秒
        )

        # 返回响应数据
        return {
            "status_code": response.status_code,
            "data": response.json() if response.content else {},
            "headers": dict(response.headers)
        }

    except requests.exceptions.Timeout:
        return {"error": "请求超时，请检查网络连接或服务器状态"}
    except requests.exceptions.ConnectionError:
        return {"error": "连接错误，请检查URL是否正确或服务器是否运行"}
    except requests.exceptions.RequestException as e:
        return {"error": f"请求异常: {str(e)}"}
    except ValueError as e:
        return {"error": f"JSON解析错误: {str(e)}", "raw_response": response.text}


def main():
    # 设置验证接口的URL
    validate_url = "http://wushuang.free.idcfengye.com/api/auth/validate"

    # 要验证的Token
    token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1NjMwMDI4MywiZXhwIjoxNzU2MzAwNTgzfQ.PZ_w7DZF6bX5BG3YBmxL-3C6l4QBNdnamwyzoKQxIcQ"

    print(f"正在验证Token: {token[:20]}...{token[-20:]}")
    print(f"验证URL: {validate_url}")
    print("-" * 50)

    # 发送验证请求
    result = validate_token(validate_url, token)

    # 处理并显示结果
    if "error" in result:
        print(f"错误: {result['error']}")
        return 1

    print(f"HTTP状态码: {result['status_code']}")

    if result['status_code'] == 200:
        print("Token验证成功!")
        print("响应数据:")
        print(json.dumps(result['data'], indent=2, ensure_ascii=False))
    else:
        print("Token验证失败!")
        print("错误信息:")
        print(json.dumps(result['data'], indent=2, ensure_ascii=False))

    return 0


if __name__ == "__main__":
    sys.exit(main())