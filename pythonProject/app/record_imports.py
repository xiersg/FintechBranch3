# record_imports.py
import sys, os, runpy, builtins, types
from pathlib import Path

# --------- helpers ---------
def get_stdlib_names():
    # 3.10+ 有 sys.stdlib_module_names
    names = set()
    if hasattr(sys, "stdlib_module_names"):
        names |= set(sys.stdlib_module_names)
    # 常见内建/伪模块兜底
    names |= {"builtins", "encodings", "importlib", "site", "sys", "typing", "zipimport"}
    return names

def top_level(modname: str) -> str:
    return modname.split(".", 1)[0]

def map_modules_to_dists(mod_roots):
    """用 importlib.metadata 把模块名映射到发行包名与版本"""
    try:
        from importlib.metadata import packages_distributions, version, PackageNotFoundError
    except Exception:
        from importlib_metadata import packages_distributions, version, PackageNotFoundError  # backport

    mapping = packages_distributions()  # {module: [dist, ...]}
    results = {}
    for m in sorted(mod_roots):
        dists = mapping.get(m) or []
        # 挑一个最可能的（通常只有一个）
        if not dists:
            continue
        dist = dists[0]
        try:
            ver = version(dist)
        except PackageNotFoundError:
            continue
        results[dist] = ver
    return results

# --------- main ----------
def main():
    if len(sys.argv) < 2:
        print("用法：python record_imports.py <your_script.py> [-- 脚本参数...]")
        sys.exit(1)

    # 拆分 “--” 之后的参数传给目标脚本
    if "--" in sys.argv:
        idx = sys.argv.index("--")
        target = sys.argv[1]
        script_args = sys.argv[idx+1:]
    else:
        target = sys.argv[1]
        script_args = []

    target_path = Path(target).resolve()
    if not target_path.exists():
        print(f"[错误] 找不到脚本：{target_path}")
        sys.exit(1)

    # 记录导入
    imported = set()
    stdlib = get_stdlib_names()

    real_import = builtins.__import__

    def tracking_import(name, globals=None, locals=None, fromlist=(), level=0):
        mod = real_import(name, globals, locals, fromlist, level)
        try:
            imported.add(top_level(mod.__name__))
        except Exception:
            pass
        return mod

    builtins.__import__ = tracking_import

    # 让目标脚本能拿到自己的参数
    saved_argv = sys.argv[:]
    sys.argv = [str(target_path)] + script_args

    # 把目标脚本目录加到 sys.path 首位（像直接执行一样）
    sys.path.insert(0, str(target_path.parent))

    try:
        runpy.run_path(str(target_path), run_name="__main__")
    finally:
        # 还原
        builtins.__import__ = real_import
        sys.argv = saved_argv
        # 清理我们加的 path
        try:
            sys.path.remove(str(target_path.parent))
        except ValueError:
            pass

    # 过滤掉标准库与内建、本地包名（粗略：当前项目顶层目录名）
    local_project_names = {Path.cwd().name, target_path.parent.name}
    third_party_roots = {
        m for m in imported
        if m not in stdlib and not m.startswith("_") and m not in local_project_names
    }

    # 映射到发行包 + 版本
    dist_map = map_modules_to_dists(third_party_roots)

    # 输出与写文件
    lines = [f"{pkg}=={ver}" for pkg, ver in sorted(dist_map.items())]
    out_file = Path("used-requirements.txt")
    out_file.write_text("\n".join(lines), encoding="utf-8")

    print("\n=== 运行期间用到的第三方依赖（已写入 used-requirements.txt） ===")
    for line in lines:
        print(line)
    print("\n提示：此列表基于“本次运行实际走到的代码路径”。不同功能路径可能会引入更多依赖。")

if __name__ == "__main__":
    main()
