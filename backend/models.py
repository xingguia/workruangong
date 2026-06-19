from database import get_connection
from auth import hash_password

def _seed_admin():
    """Create default admin account if none exists."""
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM admins")
    if cursor.fetchone()[0] == 0:
        cursor.execute(
            "INSERT INTO admins (username, password_hash, role) VALUES (%s, %s, %s)",
            ("admin", hash_password("admin123"), "super")
        )
        conn.commit()
        print("Default admin account created: admin / admin123")
    cursor.close()
    conn.close()

def init_db():
    conn = get_connection()
    cursor = conn.cursor()

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            phone VARCHAR(20) NOT NULL UNIQUE,
            nickname VARCHAR(50) DEFAULT '健身爱好者',
            password_hash VARCHAR(255) NOT NULL,
            avatar VARCHAR(500) DEFAULT NULL,
            gender VARCHAR(10) DEFAULT NULL,
            fitness_goal VARCHAR(100) DEFAULT NULL,
            height INT DEFAULT 0,
            weight FLOAT DEFAULT 0,
            body_fat FLOAT DEFAULT 0,
            waist FLOAT DEFAULT 0,
            hip FLOAT DEFAULT 0,
            initial_height INT DEFAULT 0,
            initial_weight FLOAT DEFAULT 0,
            initial_body_fat FLOAT DEFAULT 0,
            initial_waist FLOAT DEFAULT 0,
            initial_hip FLOAT DEFAULT 0,
            is_vip TINYINT(1) DEFAULT 0,
            level INT DEFAULT 1,
            vip_expire_time DATETIME DEFAULT NULL,
            workout_reminder TINYINT(1) DEFAULT 1,
            achievement_notification TINYINT(1) DEFAULT 1,
            assessment_completed TINYINT(1) DEFAULT 0,
            is_active TINYINT(1) DEFAULT 1,
            username_set TINYINT(1) DEFAULT 0,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS body_records (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            user_id BIGINT NOT NULL,
            height INT DEFAULT 0,
            weight FLOAT DEFAULT 0,
            body_fat FLOAT DEFAULT 0,
            waist FLOAT DEFAULT 0,
            hip FLOAT DEFAULT 0,
            timestamp BIGINT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS workout_records (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            user_id BIGINT NOT NULL,
            task_id BIGINT DEFAULT NULL,
            exercise_name VARCHAR(100) NOT NULL,
            timestamp BIGINT NOT NULL,
            duration INT DEFAULT 0,
            sets INT DEFAULT 0,
            reps INT DEFAULT 0,
            weight FLOAT DEFAULT 0,
            calories_burned FLOAT DEFAULT 0,
            calories FLOAT DEFAULT 0,
            notes TEXT DEFAULT NULL,
            muscle_group VARCHAR(50) DEFAULT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS training_tasks (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            user_id BIGINT NOT NULL,
            date BIGINT NOT NULL,
            name VARCHAR(200) NOT NULL,
            description TEXT DEFAULT NULL,
            duration INT DEFAULT 0,
            status VARCHAR(20) DEFAULT 'NOT_STARTED',
            exercise_type VARCHAR(30) DEFAULT 'STRENGTH',
            reps INT DEFAULT 0,
            sets INT DEFAULT 0,
            weight FLOAT DEFAULT 0,
            muscle_group VARCHAR(50) DEFAULT NULL,
            sub_muscle VARCHAR(100) DEFAULT '',
            calories_recorded TINYINT(1) DEFAULT 0,
            treadmill_speed FLOAT DEFAULT 0,
            treadmill_incline FLOAT DEFAULT 0,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS exercise_plans (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            user_id BIGINT NOT NULL,
            day_of_week INT NOT NULL,
            status VARCHAR(20) DEFAULT 'NOT_SET',
            completion_status VARCHAR(20) DEFAULT 'NOT_SET',
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
            UNIQUE KEY unique_user_day (user_id, day_of_week)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS achievements (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            user_id BIGINT NOT NULL,
            achievement_type VARCHAR(50) NOT NULL,
            unlocked TINYINT(1) DEFAULT 0,
            unlock_time BIGINT DEFAULT 0,
            displayed TINYINT(1) DEFAULT 0,
            display_position INT DEFAULT -1,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
            UNIQUE KEY unique_user_achievement (user_id, achievement_type)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS admins (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(50) NOT NULL UNIQUE,
            password_hash VARCHAR(255) NOT NULL,
            role VARCHAR(20) DEFAULT 'normal',
            is_active TINYINT(1) DEFAULT 1,
            last_login DATETIME DEFAULT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS usernames (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(50) NOT NULL UNIQUE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)


    cursor.execute("""
        CREATE TABLE IF NOT EXISTS exercises (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            muscle_group VARCHAR(50) DEFAULT NULL,
            sub_muscle VARCHAR(50) DEFAULT NULL,
            exercise_type VARCHAR(30) DEFAULT 'STRENGTH',
            cal_per_rep FLOAT DEFAULT 0,
            needs_equipment TINYINT(1) DEFAULT 0,
            description TEXT DEFAULT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS announcements (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            title VARCHAR(200) NOT NULL,
            content TEXT DEFAULT NULL,
            is_active TINYINT(1) DEFAULT 1,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    conn.commit()

    # Migration: ensure is_active column exists for users table
    try:
        cursor.execute("ALTER TABLE users ADD COLUMN is_active TINYINT(1) DEFAULT 1")
        conn.commit()
        print("Added is_active column to users table")
    except Exception:
        pass  # Column already exists

    # Migration: ensure initial_* columns exist for users table
    for col in ["initial_height", "initial_weight", "initial_body_fat", "initial_waist", "initial_hip"]:
        try:
            default_val = "0" if "height" in col else "0"
            cursor.execute(f"ALTER TABLE users ADD COLUMN {col} FLOAT DEFAULT 0")
            conn.commit()
            print(f"Added {col} column to users table")
        except Exception:
            pass  # Column already exists

    # Migration: ensure exercises table has sub_muscle and needs_equipment columns
    try:
        cursor.execute("ALTER TABLE exercises ADD COLUMN sub_muscle VARCHAR(50) DEFAULT NULL")
        conn.commit()
        print("Added sub_muscle column to exercises table")
    except Exception:
        pass  # Column already exists

    try:
        cursor.execute("ALTER TABLE exercises ADD COLUMN needs_equipment TINYINT(1) DEFAULT 0")
        conn.commit()
        print("Added needs_equipment column to exercises table")
    except Exception:
        pass  # Column already exists

    # Ensure all existing users have is_active = 1
    try:
        cursor.execute("UPDATE users SET is_active = 1 WHERE is_active IS NULL")
        conn.commit()
    except Exception:
        pass

    # Migration: translate old fitness_goal values to unified Chinese
    try:
        goal_map = {
            'fat_loss': '减脂', 'muscle_gain': '增肌', 'shaping': '塑形',
            'posture': '体态', 'fitness': '增肌', 'rehab': '塑形',
            '增强体质': '增肌', '康复训练': '塑形',
        }
        for old, new in goal_map.items():
            cursor.execute("UPDATE users SET fitness_goal=%s WHERE fitness_goal=%s", (new, old))
        conn.commit()
    except Exception:
        pass

    cursor.close()
    conn.close()

    # Seed default admin account
    _seed_admin()
    print("Database tables created successfully!")

if __name__ == "__main__":
    init_db()
