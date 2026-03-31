from app import app

if __name__ == "__main__":
    # 启动Flask服务
    # debug=True 仅开发环境使用，生产环境需关闭
    app.run(
        debug=True,
        host="0.0.0.0",  # 允许外部访问
        port=5000,       # 端口可自定义
        threaded=True    # 开启多线程处理请求
    )