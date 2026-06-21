from PIL import Image, ImageDraw
import math

def draw_flame(draw, cx, cy, size, color):
    """绘制一个简约火焰图案"""
    # 火焰主体 - 使用多个叠加的形状
    # 底部宽，顶部尖

    # 主火焰（中间最大的）
    points_main = [
        (cx, cy - size * 0.5),          # 顶部尖端
        (cx + size * 0.25, cy + size * 0.1),   # 右侧中间
        (cx + size * 0.35, cy + size * 0.35),  # 右下
        (cx, cy + size * 0.5),           # 底部中心
        (cx - size * 0.35, cy + size * 0.35),  # 左下
        (cx - size * 0.25, cy + size * 0.1),   # 左侧中间
    ]
    draw.polygon(points_main, fill=color)

    # 左侧小火焰
    points_left = [
        (cx - size * 0.15, cy - size * 0.2),
        (cx - size * 0.35, cy + size * 0.05),
        (cx - size * 0.25, cy + size * 0.25),
        (cx - size * 0.05, cy + size * 0.15),
    ]
    draw.polygon(points_left, fill=color)

    # 右侧小火焰
    points_right = [
        (cx + size * 0.15, cy - size * 0.2),
        (cx + size * 0.35, cy + size * 0.05),
        (cx + size * 0.25, cy + size * 0.25),
        (cx + size * 0.05, cy + size * 0.15),
    ]
    draw.polygon(points_right, fill=color)

    # 内部高光（浅色，增加层次感）
    highlight_color = (255, 180, 100)  # 浅橙色
    points_inner = [
        (cx, cy - size * 0.15),
        (cx + size * 0.1, cy + size * 0.15),
        (cx, cy + size * 0.3),
        (cx - size * 0.1, cy + size * 0.15),
    ]
    draw.polygon(points_inner, fill=highlight_color)


def create_favicon():
    # 品牌橙色
    brand_orange = (255, 107, 53)  # #FF6B35

    # 创建多个尺寸的图标
    sizes = [16, 32, 48, 64]
    images = []

    for size in sizes:
        # 创建透明背景
        img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)

        # 绘制圆角方形背景
        margin = max(1, size // 8)
        corner_radius = max(2, size // 5)

        # 绘制圆角矩形背景
        draw.rounded_rectangle(
            [margin, margin, size - margin - 1, size - margin - 1],
            radius=corner_radius,
            fill=brand_orange
        )

        # 在中心绘制火焰
        center_x = size // 2
        center_y = size // 2
        flame_size = size * 0.7

        # 火焰用白色绘制
        draw_flame(draw, center_x, center_y, flame_size, (255, 255, 255))

        images.append(img)

    # 保存为ICO文件（包含多个尺寸）
    output_path = 'z:/ruangong/backend/static/favicon.ico'
    images[1].save(
        output_path,
        format='ICO',
        sizes=[(s, s) for s in sizes],
        append_images=images[0:1] + images[2:]
    )
    print(f'Favicon已生成: {output_path}')
    print(f'包含尺寸: {sizes}')

    # 同时保存一个PNG预览
    preview_path = 'z:/ruangong/backend/static/favicon_preview.png'
    images[2].save(preview_path)
    print(f'PNG预览已保存: {preview_path}')


if __name__ == '__main__':
    create_favicon()
