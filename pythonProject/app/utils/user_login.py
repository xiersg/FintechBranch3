# user_utils.py
import hashlib

# 简易的内存用户表：key=username, value=sha256(password)
_USERS = {}

def _hash_password(password: str) -> str:
    """用SHA256哈希密码（比明文安全）"""
    return hashlib.sha256(password.encode("utf-8")).hexdigest()

def register(username: str, password: str) -> tuple[bool, str]:
    """
    注册用户
    返回: (成功?, 提示信息)
    """
    if username in _USERS:
        return False, "用户名已存在"
    _USERS[username] = _hash_password(password)
    return True, "注册成功"

def login(username: str, password: str) -> tuple[bool, str]:
    """
    登录用户
    返回: (成功?, 提示信息)
    """
    if username not in _USERS:
        return False, "用户不存在"
    if _USERS[username] != _hash_password(password):
        return False, "密码错误"
    return True, "登录成功"
