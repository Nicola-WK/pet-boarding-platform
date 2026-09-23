-- Run against the pet_boarding database. Future Flyway runs this once.
-- Primary keys are assigned by the application ID generator in later phases.

CREATE TABLE `user` (
  `id` BIGINT NOT NULL,
  `openid` VARCHAR(64) NOT NULL,
  `unionid` VARCHAR(64) NULL,
  `nickname` VARCHAR(80) NOT NULL DEFAULT '',
  `avatar` VARCHAR(1024) NULL,
  `phone` VARCHAR(32) NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 active',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_openid` (`openid`),
  UNIQUE KEY `uk_user_unionid` (`unionid`),
  KEY `idx_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pet` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `name` VARCHAR(80) NOT NULL,
  `avatar` VARCHAR(1024) NULL,
  `type` VARCHAR(32) NOT NULL COMMENT 'dog, cat, or other',
  `breed` VARCHAR(80) NULL,
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '0 unknown, 1 male, 2 female',
  `birthday` DATETIME NULL,
  `weight` DECIMAL(6,2) NULL COMMENT 'kg',
  `vaccine` TEXT NULL,
  `character` TEXT NULL,
  `diet` TEXT NULL,
  `remark` TEXT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet_user_deleted` (`user_id`, `is_deleted`),
  CONSTRAINT `fk_pet_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `provider` (
  `id` BIGINT NOT NULL,
  `owner_user_id` BIGINT NOT NULL,
  `name` VARCHAR(120) NOT NULL,
  `contact_phone` VARCHAR(32) NOT NULL,
  `address` VARCHAR(255) NOT NULL,
  `latitude` DECIMAL(10,7) NULL,
  `longitude` DECIMAL(10,7) NULL,
  `description` TEXT NULL,
  `images` JSON NULL COMMENT 'COS image URLs',
  `license_image` VARCHAR(1024) NULL,
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending, 1 approved, 2 rejected',
  `audit_reason` VARCHAR(500) NULL,
  `audited_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_owner` (`owner_user_id`),
  KEY `idx_provider_audit_created` (`audit_status`, `created_at`),
  CONSTRAINT `fk_provider_owner` FOREIGN KEY (`owner_user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `boarding_service` (
  `id` BIGINT NOT NULL,
  `provider_id` BIGINT NOT NULL,
  `name` VARCHAR(120) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL COMMENT 'price per boarding day in CNY',
  `pet_type` VARCHAR(32) NOT NULL,
  `capacity` INT NOT NULL,
  `description` TEXT NULL,
  `images` JSON NULL COMMENT 'COS image URLs',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0 inactive, 1 active',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_service_provider_status` (`provider_id`, `status`),
  KEY `idx_service_type_status_price` (`pet_type`, `status`, `price`),
  CONSTRAINT `fk_service_provider` FOREIGN KEY (`provider_id`) REFERENCES `provider` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_service_price` CHECK (`price` >= 0),
  CONSTRAINT `chk_service_capacity` CHECK (`capacity` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `boarding_order` (
  `id` BIGINT NOT NULL,
  `order_no` VARCHAR(40) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `pet_id` BIGINT NOT NULL,
  `provider_id` BIGINT NOT NULL,
  `service_id` BIGINT NOT NULL,
  `start_at` DATETIME NOT NULL,
  `end_at` DATETIME NOT NULL,
  `boarding_days` INT NOT NULL,
  `unit_price` DECIMAL(10,2) NOT NULL COMMENT 'price snapshot',
  `total_amount` DECIMAL(10,2) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0 unpaid, 1 pending confirmation, 2 boarding, 3 completed, 4 canceled',
  `remark` VARCHAR(500) NULL,
  `expires_at` DATETIME NULL COMMENT 'unpaid order payment deadline',
  `paid_at` DATETIME NULL,
  `confirmed_at` DATETIME NULL,
  `completed_at` DATETIME NULL,
  `canceled_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user_status_created` (`user_id`, `status`, `created_at`),
  KEY `idx_order_provider_status_start` (`provider_id`, `status`, `start_at`),
  KEY `idx_order_pet` (`pet_id`),
  KEY `idx_order_service` (`service_id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_order_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_order_provider` FOREIGN KEY (`provider_id`) REFERENCES `provider` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_order_service` FOREIGN KEY (`service_id`) REFERENCES `boarding_service` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_order_dates` CHECK (`end_at` > `start_at`),
  CONSTRAINT `chk_order_days` CHECK (`boarding_days` > 0),
  CONSTRAINT `chk_order_amount` CHECK (`unit_price` >= 0 AND `total_amount` >= 0),
  CONSTRAINT `chk_order_status` CHECK (`status` IN (0, 1, 2, 3, 4))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payment_record` (
  `id` BIGINT NOT NULL,
  `order_id` BIGINT NOT NULL,
  `out_trade_no` VARCHAR(64) NOT NULL,
  `wechat_transaction_id` VARCHAR(64) NULL,
  `prepay_id` VARCHAR(128) NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0 created, 1 paid, 2 failed, 3 closed, 4 refunded',
  `paid_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_out_trade_no` (`out_trade_no`),
  UNIQUE KEY `uk_payment_wechat_transaction` (`wechat_transaction_id`),
  KEY `idx_payment_order_status` (`order_id`, `status`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `boarding_order` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_payment_amount` CHECK (`amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `review` (
  `id` BIGINT NOT NULL,
  `order_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `provider_id` BIGINT NOT NULL,
  `service_id` BIGINT NOT NULL,
  `score` TINYINT NOT NULL,
  `content` TEXT NULL,
  `images` JSON NULL COMMENT 'COS image URLs',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_order` (`order_id`),
  KEY `idx_review_provider_created` (`provider_id`, `created_at`),
  KEY `idx_review_service_created` (`service_id`, `created_at`),
  KEY `idx_review_user_created` (`user_id`, `created_at`),
  CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `boarding_order` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_review_provider` FOREIGN KEY (`provider_id`) REFERENCES `provider` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_review_service` FOREIGN KEY (`service_id`) REFERENCES `boarding_service` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_review_score` CHECK (`score` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `provider_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_provider` (`user_id`, `provider_id`),
  KEY `idx_favorite_provider` (`provider_id`),
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_favorite_provider` FOREIGN KEY (`provider_id`) REFERENCES `provider` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(40) NOT NULL,
  `title` VARCHAR(120) NOT NULL,
  `content` TEXT NOT NULL,
  `related_order_id` BIGINT NULL,
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `read_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notification_user_read_created` (`user_id`, `is_read`, `created_at`),
  KEY `idx_notification_order` (`related_order_id`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_notification_order` FOREIGN KEY (`related_order_id`) REFERENCES `boarding_order` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
