-- HappyStay ホテル予約システムのデータベース
-- このSQLは、表・連番・トリガー・検索用インデックスを作成し、サンプルデータを登録します。

CREATE SEQUENCE sn_users_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sn_hotels_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sn_room_types_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sn_reservations_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sn_taxi_bookings_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sn_reviews_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sn_notifications_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sn_emailver_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sn_pwreset_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sn_acctdel_seq START WITH 1 INCREMENT BY 1 NOCACHE;


CREATE TABLE sn_users (
    id                 NUMBER(10) NOT NULL,
    full_name          VARCHAR2(100 CHAR) NOT NULL,
    email              VARCHAR2(254) NOT NULL,
    password_hash      VARCHAR2(100) NOT NULL,
    phone              VARCHAR2(30),
    address            VARCHAR2(255 CHAR),
    role               VARCHAR2(10) DEFAULT 'CUSTOMER' NOT NULL,
    email_verified     NUMBER(1) DEFAULT 0 NOT NULL,
    lang               VARCHAR2(5) DEFAULT 'en' NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_users PRIMARY KEY (id),
    CONSTRAINT uq_sn_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_lower CHECK (email = LOWER(email)),
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER','ADMIN')),
    CONSTRAINT ck_users_verified CHECK (email_verified IN (0,1)),
    CONSTRAINT ck_users_lang CHECK (lang IN ('en','ja','ne','si','my'))
);


CREATE TABLE sn_hotels (
    id                 NUMBER(10) NOT NULL,
    name               VARCHAR2(150 CHAR) NOT NULL,
    city               VARCHAR2(100 CHAR) NOT NULL,
    address            VARCHAR2(255 CHAR),
    description        VARCHAR2(1000 CHAR),
    stars              NUMBER(1) DEFAULT 3 NOT NULL,
    rating             NUMBER(2,1) DEFAULT 0 NOT NULL,
    image_url          VARCHAR2(500),
    amenities          VARCHAR2(500 CHAR),
    active             NUMBER(1) DEFAULT 1 NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_hotels PRIMARY KEY (id),
    CONSTRAINT ck_hotels_stars CHECK (stars BETWEEN 1 AND 5),
    CONSTRAINT ck_hotels_rating CHECK (rating BETWEEN 0 AND 5),
    CONSTRAINT ck_hotels_active CHECK (active IN (0,1))
);


CREATE TABLE sn_room_types (
    id                 NUMBER(10) NOT NULL,
    hotel_id           NUMBER(10) NOT NULL,
    name               VARCHAR2(100 CHAR) NOT NULL,
    description        VARCHAR2(1000 CHAR),
    price_per_night    NUMBER(12,2) NOT NULL,
    capacity           NUMBER(3) DEFAULT 2 NOT NULL,
    total_rooms        NUMBER(5) DEFAULT 1 NOT NULL,
    image_url          VARCHAR2(500),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_room_types PRIMARY KEY (id),
    CONSTRAINT fk_room_hotel FOREIGN KEY (hotel_id) REFERENCES sn_hotels (id) ON DELETE CASCADE,
    CONSTRAINT ck_room_price CHECK (price_per_night >= 0),
    CONSTRAINT ck_room_capacity CHECK (capacity >= 1),
    CONSTRAINT ck_room_total CHECK (total_rooms >= 0)
);

-- sn_reservations: Bookings. user_id becomes NULL when the guest deletes the account (the history stays). card_last4 holds only the masked number.
CREATE TABLE sn_reservations (
    id                 NUMBER(10) NOT NULL,
    user_id            NUMBER(10),
    room_type_id       NUMBER(10) NOT NULL,
    check_in           DATE NOT NULL,
    check_out          DATE NOT NULL,
    rooms_count        NUMBER(3) DEFAULT 1 NOT NULL,
    guests             NUMBER(4) DEFAULT 1 NOT NULL,
    nights             NUMBER(3) NOT NULL,
    total_amount       NUMBER(12,2) NOT NULL,
    guest_name         VARCHAR2(100 CHAR) NOT NULL,
    guest_phone        VARCHAR2(30),
    special_requests   VARCHAR2(1000 CHAR),
    payment_method     VARCHAR2(20) DEFAULT 'PAY_AT_HOTEL' NOT NULL,
    card_last4         VARCHAR2(30),
    status             VARCHAR2(12) DEFAULT 'PENDING' NOT NULL,
    reminder_sent      NUMBER(1) DEFAULT 0 NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_reservations PRIMARY KEY (id),
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES sn_users (id) ON DELETE SET NULL,
    CONSTRAINT fk_res_room FOREIGN KEY (room_type_id) REFERENCES sn_room_types (id),
    CONSTRAINT ck_res_dates CHECK (check_out > check_in),
    CONSTRAINT ck_res_rooms CHECK (rooms_count >= 1),
    CONSTRAINT ck_res_guests CHECK (guests >= 1),
    CONSTRAINT ck_res_payment CHECK (payment_method IN ('PAY_AT_HOTEL','CREDIT_CARD','PAYPAY','BANK_TRANSFER')),
    CONSTRAINT ck_res_status CHECK (status IN ('PENDING','CONFIRMED','CHECKED_IN','COMPLETED','CANCELLED')),
    CONSTRAINT ck_res_reminder CHECK (reminder_sent IN (0,1))
);

-- sn_taxi_bookings: Taxi rides to the booked hotel.
CREATE TABLE sn_taxi_bookings (
    id                 NUMBER(10) NOT NULL,
    user_id            NUMBER(10),
    reservation_id     NUMBER(10) NOT NULL,
    pickup_type        VARCHAR2(10) NOT NULL,
    pickup_location    VARCHAR2(255 CHAR) NOT NULL,
    pickup_time        TIMESTAMP NOT NULL,
    passengers         NUMBER(2) DEFAULT 1 NOT NULL,
    luggage            NUMBER(2) DEFAULT 0 NOT NULL,
    car_type           VARCHAR2(10) NOT NULL,
    estimated_fare     NUMBER(12,2) NOT NULL,
    notes              VARCHAR2(500 CHAR),
    status             VARCHAR2(12) DEFAULT 'REQUESTED' NOT NULL,
    driver_name        VARCHAR2(100 CHAR),
    driver_phone       VARCHAR2(30),
    reminder_sent      NUMBER(1) DEFAULT 0 NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_taxi_bookings PRIMARY KEY (id),
    CONSTRAINT fk_taxi_user FOREIGN KEY (user_id) REFERENCES sn_users (id) ON DELETE SET NULL,
    CONSTRAINT fk_taxi_res FOREIGN KEY (reservation_id) REFERENCES sn_reservations (id) ON DELETE CASCADE,
    CONSTRAINT ck_taxi_zone CHECK (pickup_type IN ('AIRPORT','STATION','OTHER')),
    CONSTRAINT ck_taxi_car CHECK (car_type IN ('STANDARD','PREMIUM','VAN')),
    CONSTRAINT ck_taxi_status CHECK (status IN ('REQUESTED','CONFIRMED','COMPLETED','CANCELLED')),
    CONSTRAINT ck_taxi_people CHECK (passengers >= 1 AND luggage >= 0),
    CONSTRAINT ck_taxi_reminder CHECK (reminder_sent IN (0,1))
);

-- sn_reviews: One review per guest per hotel (the app uses MERGE on hotel_id + user_id).
CREATE TABLE sn_reviews (
    id                 NUMBER(10) NOT NULL,
    hotel_id           NUMBER(10) NOT NULL,
    user_id            NUMBER(10) NOT NULL,
    rating             NUMBER(1) NOT NULL,
    comment_text       VARCHAR2(1000 CHAR),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_reviews PRIMARY KEY (id),
    CONSTRAINT fk_rev_hotel FOREIGN KEY (hotel_id) REFERENCES sn_hotels (id) ON DELETE CASCADE,
    CONSTRAINT fk_rev_user FOREIGN KEY (user_id) REFERENCES sn_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_rev_hotel_user UNIQUE (hotel_id, user_id),
    CONSTRAINT ck_rev_rating CHECK (rating BETWEEN 1 AND 5)
);

-- sn_notifications: In-site notifications (the bell icon).
CREATE TABLE sn_notifications (
    id                 NUMBER(10) NOT NULL,
    user_id            NUMBER(10) NOT NULL,
    message            VARCHAR2(500 CHAR) NOT NULL,
    link               VARCHAR2(255),
    is_read            NUMBER(1) DEFAULT 0 NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES sn_users (id) ON DELETE CASCADE,
    CONSTRAINT ck_notif_read CHECK (is_read IN (0,1))
);

-- sn_email_verification_codes: 6-digit codes for e-mail verification after registration (valid 15 minutes, one use).
CREATE TABLE sn_email_verification_codes (
    id                 NUMBER(10) NOT NULL,
    user_id            NUMBER(10) NOT NULL,
    code               VARCHAR2(10) NOT NULL,
    used               NUMBER(1) DEFAULT 0 NOT NULL,
    expires_at         TIMESTAMP NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_emailver PRIMARY KEY (id),
    CONSTRAINT fk_emailver_user FOREIGN KEY (user_id) REFERENCES sn_users (id) ON DELETE CASCADE,
    CONSTRAINT ck_emailver_used CHECK (used IN (0,1))
);

-- sn_password_reset_codes: 6-digit codes for forgot-password and change-password (valid 10 minutes, one use).
CREATE TABLE sn_password_reset_codes (
    id                 NUMBER(10) NOT NULL,
    user_id            NUMBER(10) NOT NULL,
    code               VARCHAR2(10) NOT NULL,
    used               NUMBER(1) DEFAULT 0 NOT NULL,
    expires_at         TIMESTAMP NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_pwreset PRIMARY KEY (id),
    CONSTRAINT fk_pwreset_user FOREIGN KEY (user_id) REFERENCES sn_users (id) ON DELETE CASCADE,
    CONSTRAINT ck_pwreset_used CHECK (used IN (0,1))
);

-- sn_account_deletion_codes: 6-digit codes to confirm account deletion (valid 10 minutes, one use).
CREATE TABLE sn_account_deletion_codes (
    id                 NUMBER(10) NOT NULL,
    user_id            NUMBER(10) NOT NULL,
    code               VARCHAR2(10) NOT NULL,
    used               NUMBER(1) DEFAULT 0 NOT NULL,
    expires_at         TIMESTAMP NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_sn_acctdel PRIMARY KEY (id),
    CONSTRAINT fk_acctdel_user FOREIGN KEY (user_id) REFERENCES sn_users (id) ON DELETE CASCADE,
    CONSTRAINT ck_acctdel_used CHECK (used IN (0,1))
);


CREATE INDEX ix_hotels_city ON sn_hotels (city);
CREATE INDEX ix_room_hotel ON sn_room_types (hotel_id);
CREATE INDEX ix_res_user ON sn_reservations (user_id);
CREATE INDEX ix_res_room_dates ON sn_reservations (room_type_id, check_in, check_out);
CREATE INDEX ix_res_status ON sn_reservations (status);
CREATE INDEX ix_taxi_user ON sn_taxi_bookings (user_id);
CREATE INDEX ix_taxi_res ON sn_taxi_bookings (reservation_id);
CREATE INDEX ix_taxi_status_time ON sn_taxi_bookings (status, pickup_time);
CREATE INDEX ix_notif_user ON sn_notifications (user_id, created_at);
CREATE INDEX ix_emailver ON sn_email_verification_codes (user_id, code);
CREATE INDEX ix_pwreset ON sn_password_reset_codes (user_id, code);
CREATE INDEX ix_acctdel ON sn_account_deletion_codes (user_id, code);


-- ユーザー登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_users_bi
BEFORE INSERT ON sn_users
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_users_seq.NEXTVAL;
END;
/

-- ホテル登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_hotels_bi
BEFORE INSERT ON sn_hotels
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_hotels_seq.NEXTVAL;
END;
/

-- 部屋タイプ登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_room_types_bi
BEFORE INSERT ON sn_room_types
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_room_types_seq.NEXTVAL;
END;
/

-- 予約登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_reservations_bi
BEFORE INSERT ON sn_reservations
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_reservations_seq.NEXTVAL;
END;
/

-- タクシー予約登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_taxi_bookings_bi
BEFORE INSERT ON sn_taxi_bookings
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_taxi_bookings_seq.NEXTVAL;
END;
/

-- 口コミ登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_reviews_bi
BEFORE INSERT ON sn_reviews
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_reviews_seq.NEXTVAL;
END;
/

-- 通知登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_notifications_bi
BEFORE INSERT ON sn_notifications
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_notifications_seq.NEXTVAL;
END;
/

-- メール認証コード登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_emailver_bi
BEFORE INSERT ON sn_email_verification_codes
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_emailver_seq.NEXTVAL;
END;
/

-- パスワード再設定コード登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_pwreset_bi
BEFORE INSERT ON sn_password_reset_codes
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_pwreset_seq.NEXTVAL;
END;
/

-- アカウント削除コード登録時、IDがない場合に自動でIDを設定します。
CREATE OR REPLACE TRIGGER sn_acctdel_bi
BEFORE INSERT ON sn_account_deletion_codes
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := sn_acctdel_seq.NEXTVAL;
END;
/



INSERT INTO sn_users (full_name, email, password_hash, role, email_verified, lang)
SELECT 'HappyStay Admin', 'karkichakra12354@gmail.com', 'de82b9f3ab8bba41bc45dab3592d51abdbe5cdbe438ee9ba86f47e2bc760386a', 'ADMIN', 1, 'en' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sn_users WHERE role = 'ADMIN');

INSERT INTO sn_hotels (name, city, address, description, stars, image_url, amenities, active)
SELECT 'Himalaya View Hotel', 'Kathmandu', 'Thamel Marg 12, Kathmandu', 'Comfortable hotel in the middle of Thamel, a short walk from shops, restaurants and Durbar Square.', 4, 'https://picsum.photos/seed/happystay-ktm/800/500', 'Free Wi-Fi, Breakfast, Airport pickup, Rooftop cafe', 1 FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sn_hotels WHERE name = 'Himalaya View Hotel');
INSERT INTO sn_room_types (hotel_id, name, description, price_per_night, capacity, total_rooms)
SELECT h.id, 'Standard Room', 'Queen bed, city view, private bathroom.', 4500, 2, 10 FROM sn_hotels h
WHERE h.name = 'Himalaya View Hotel' AND NOT EXISTS (SELECT 1 FROM sn_room_types x WHERE x.hotel_id = h.id AND x.name = 'Standard Room');
INSERT INTO sn_room_types (hotel_id, name, description, price_per_night, capacity, total_rooms)
SELECT h.id, 'Deluxe Room', 'Larger room with balcony and mountain view.', 7200, 3, 6 FROM sn_hotels h
WHERE h.name = 'Himalaya View Hotel' AND NOT EXISTS (SELECT 1 FROM sn_room_types x WHERE x.hotel_id = h.id AND x.name = 'Deluxe Room');
INSERT INTO sn_room_types (hotel_id, name, description, price_per_night, capacity, total_rooms)
SELECT h.id, 'Family Suite', 'Two bedrooms and a living area for up to 5 guests.', 12500, 5, 3 FROM sn_hotels h
WHERE h.name = 'Himalaya View Hotel' AND NOT EXISTS (SELECT 1 FROM sn_room_types x WHERE x.hotel_id = h.id AND x.name = 'Family Suite');

INSERT INTO sn_hotels (name, city, address, description, stars, image_url, amenities, active)
SELECT 'Lakeside Retreat', 'Pokhara', 'Lakeside Road 5, Pokhara', 'Quiet resort on Phewa Lake with a garden, pool and sunrise views of the Annapurna range.', 5, 'https://picsum.photos/seed/happystay-pkr/800/500', 'Free Wi-Fi, Pool, Breakfast, Spa, Parking', 1 FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sn_hotels WHERE name = 'Lakeside Retreat');
INSERT INTO sn_room_types (hotel_id, name, description, price_per_night, capacity, total_rooms)
SELECT h.id, 'Garden Room', 'Ground floor room opening to the garden.', 6800, 2, 8 FROM sn_hotels h
WHERE h.name = 'Lakeside Retreat' AND NOT EXISTS (SELECT 1 FROM sn_room_types x WHERE x.hotel_id = h.id AND x.name = 'Garden Room');
INSERT INTO sn_room_types (hotel_id, name, description, price_per_night, capacity, total_rooms)
SELECT h.id, 'Lake View Suite', 'Suite with a private terrace facing the lake.', 15800, 3, 4 FROM sn_hotels h
WHERE h.name = 'Lakeside Retreat' AND NOT EXISTS (SELECT 1 FROM sn_room_types x WHERE x.hotel_id = h.id AND x.name = 'Lake View Suite');

INSERT INTO sn_hotels (name, city, address, description, stars, image_url, amenities, active)
SELECT 'Sakura Business Hotel', 'Tokyo', '1-2-3 Shinjuku, Shinjuku-ku, Tokyo', 'Compact, clean business hotel five minutes from Shinjuku Station.', 3, 'https://picsum.photos/seed/happystay-tyo/800/500', 'Free Wi-Fi, Breakfast, Laundry, Non-smoking rooms', 1 FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sn_hotels WHERE name = 'Sakura Business Hotel');
INSERT INTO sn_room_types (hotel_id, name, description, price_per_night, capacity, total_rooms)
SELECT h.id, 'Single Room', 'Single bed, desk and private bathroom.', 9000, 1, 12 FROM sn_hotels h
WHERE h.name = 'Sakura Business Hotel' AND NOT EXISTS (SELECT 1 FROM sn_room_types x WHERE x.hotel_id = h.id AND x.name = 'Single Room');
INSERT INTO sn_room_types (hotel_id, name, description, price_per_night, capacity, total_rooms)
SELECT h.id, 'Twin Room', 'Two beds, suitable for two guests.', 13500, 2, 8 FROM sn_hotels h
WHERE h.name = 'Sakura Business Hotel' AND NOT EXISTS (SELECT 1 FROM sn_room_types x WHERE x.hotel_id = h.id AND x.name = 'Twin Room');

-- ここまでの登録内容を確定します。
COMMIT;

