-- PostgreSQL DDL Script

-- 1. ユーザー基本テーブル
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    device_token VARCHAR(255),
    timezone VARCHAR(50) DEFAULT 'Asia/Tokyo',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. アプリ利用状況ログ (Action提案の分析用)
CREATE TABLE user_app_usages (
    usage_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    package_name VARCHAR(255) NOT NULL,
    app_category VARCHAR(100),
    duration_seconds INT NOT NULL,
    last_opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_app_usages_user_time ON user_app_usages(user_id, last_opened_at DESC);

-- 3. SNS通知パース・予定検出イベントテーブル
CREATE TABLE detected_events (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    source_app VARCHAR(100) NOT NULL, -- 'LINE', 'Messenger' など
    sender VARCHAR(100),
    raw_text TEXT NOT NULL,
    parsed_title VARCHAR(255) NOT NULL,
    start_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    end_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    location VARCHAR(255),
    has_conflict BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_detected_events_user_status ON detected_events(user_id, status);

-- 4. カレンダー・週末提案スケジュールテーブル
CREATE TABLE schedules (
    schedule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    end_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    location VARCHAR(255),
    is_ai_suggested BOOLEAN DEFAULT FALSE,
    deep_link_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. おすすめAction提案テーブル
CREATE TABLE action_recommendations (
    recommendation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL, -- 'RESTAURANT_RESERVATION', 'ARTICLE_SUMMARY' など
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    reason TEXT,
    deep_link_url TEXT,
    is_dismissed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. 学習プラン（親ヘッダー）
CREATE TABLE learning_plans (
    plan_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    topic VARCHAR(255) NOT NULL,
    target_goal TEXT,
    total_steps INT NOT NULL DEFAULT 0,
    completed_steps INT NOT NULL DEFAULT 0,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ARCHIVED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. 学習モジュール・ガイドブック（子明細・エビデンス保持）
CREATE TABLE learning_modules (
    module_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID NOT NULL REFERENCES learning_plans(plan_id) ON DELETE CASCADE,
    step_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content_markdown TEXT NOT NULL,
    evidences JSONB DEFAULT '[]'::jsonb, -- 根拠URL, 論文タイトル, 一次情報ソース
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_plan_step UNIQUE (plan_id, step_number)
);