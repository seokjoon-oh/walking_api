-- Walking Trail REST API
-- MySQL 8.x sample schema and project sample data
-- Original project used a larger public walking-trail dataset.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS walk_tags;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS walk3;
DROP TABLE IF EXISTS walk2;
DROP TABLE IF EXISTS walk1;

CREATE TABLE walk1 (
    id varchar(10) NOT NULL,
    course_id varchar(30) DEFAULT NULL,
    trail_name varchar(200) DEFAULT NULL,
    course_name varchar(200) DEFAULT NULL,
    course_desc text,
    district_name varchar(100) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE walk2 (
    id varchar(10) NOT NULL,
    course_id varchar(30) DEFAULT NULL,
    difficulty varchar(30) DEFAULT NULL,
    course_km double DEFAULT NULL,
    hour int DEFAULT NULL,
    minute int DEFAULT NULL,
    minutes int DEFAULT NULL,
    water varchar(10) DEFAULT NULL,
    toilet varchar(10) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE walk3 (
    id varchar(10) NOT NULL,
    course_id varchar(30) DEFAULT NULL,
    address varchar(300) DEFAULT NULL,
    lat double DEFAULT NULL,
    lng double DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE favorites (
    favorite_id int NOT NULL AUTO_INCREMENT,
    user_id varchar(20) NOT NULL,
    walk_id varchar(10) NOT NULL,
    created_at datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (favorite_id),
    UNIQUE KEY uq_favorites_user_walk (user_id, walk_id),
    KEY idx_favorites_walk_id (walk_id),
    CONSTRAINT favorites_ibfk_1 FOREIGN KEY (walk_id) REFERENCES walk1 (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reviews (
    review_id int NOT NULL AUTO_INCREMENT,
    walk_id varchar(10) NOT NULL,
    user_id varchar(20) NOT NULL,
    rating int DEFAULT NULL,
    content varchar(500) DEFAULT NULL,
    created_at datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (review_id),
    UNIQUE KEY uq_reviews_user_walk (user_id, walk_id),
    KEY idx_reviews_walk_id (walk_id),
    CONSTRAINT reviews_ibfk_1 FOREIGN KEY (walk_id) REFERENCES walk1 (id),
    CONSTRAINT reviews_chk_1 CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE walk_tags (
    tag_id int NOT NULL AUTO_INCREMENT,
    walk_id varchar(10) NOT NULL,
    tag_name varchar(50) NOT NULL,
    PRIMARY KEY (tag_id),
    UNIQUE KEY uq_walk_tags_walk_tag (walk_id, tag_name),
    CONSTRAINT walk_tags_ibfk_1 FOREIGN KEY (walk_id) REFERENCES walk1 (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELIMITER //
CREATE TRIGGER trg_review_default_content
BEFORE INSERT ON reviews
FOR EACH ROW
BEGIN
    IF NEW.content IS NULL OR TRIM(NEW.content) = '' THEN
        SET NEW.content = '후기 미작성';
    END IF;
END //
DELIMITER ;

-- Sample rows extracted from the project dataset.
INSERT INTO walk1 (id, course_id, trail_name, course_name, course_desc, district_name) VALUES
('K001','KCCWSPO20N000000001','남산 녹색 둘레길','남산 녹색 둘레길','지천생태길 ~ 녹색길 ~ 벚꽃길 ~ 고향길','충남 청양군'),
('K002','KCCWSPO20N000000002','거북이 마을 솔바람길','01코스','전통체험관~보살바위~말바위~자라바위~전용석고택~호랑이가 잡아준묘~사랑바위~전통체험관(2.1km)','충남 홍성군'),
('K003','KCCWSPO20N000000003','거북이 마을 솔바람길','02코스','자라바위~북방성황당~범바위~할매바위~삼형제바위~호랑이굴바위~산제바위~사랑바위(2.6km)','충남 홍성군'),
('K004','KCCWSPO20N000000004','거북이 마을 솔바람길','03코스','삼형제바위 ~ 곰보바위 ~ 감투봉 ~ 전통체험관(2.8km)','충남 홍성군'),
('K005','KCCWSPO20N000000005','내포문화숲길','내포 역사인물길','용봉산~고암이응노생가~백월산~남산~보개산~상지천~광천읍내~옹암리 새우젓마을~오서산 담산리 주차장','충남 홍성군');

INSERT INTO walk2 (id, course_id, difficulty, course_km, hour, minute, minutes, water, toilet) VALUES
('K001','KCCWSPO20N000000001','쉬움',13.8,4,0,240,'yes','yes'),
('K002','KCCWSPO20N000000002','보통',2.1,1,0,60,'yes','yes'),
('K003','KCCWSPO20N000000003','보통',2.1,1,0,60,'yes','yes'),
('K004','KCCWSPO20N000000004','보통',2.1,1,0,60,'yes','yes'),
('K005','KCCWSPO20N000000005','보통',50.5,23,0,1380,'yes','yes');

INSERT INTO walk3 (id, course_id, address, lat, lng) VALUES
('K001','KCCWSPO20N000000001','충남 청양군 청양읍 적누리 산 18-52',36.426217,126.810638),
('K002','KCCWSPO20N000000002','충남 홍성군 구항면 내현리 353-1',36.571487,126.616842),
('K003','KCCWSPO20N000000003','충남 홍성군 구항면 내현리 353-1',36.571487,126.616842),
('K004','KCCWSPO20N000000004','충남 홍성군 구항면 내현리 353-1',36.571487,126.616842),
('K005','KCCWSPO20N000000005','충남 예산군 덕산면 상가리 298',36.714944,126.630901);

INSERT INTO walk_tags (walk_id, tag_name) VALUES
('K001','숲길'),('K001','운동추천'),('K001','장거리'),('K001','초보추천'),
('K002','가족추천'),('K002','짧은코스'),('K002','초보추천'),
('K003','겨울'),('K003','마을길'),('K003','조용한길'),
('K004','가족추천'),('K004','데이트코스'),('K004','야경'),
('K005','강변길'),('K005','봄꽃길'),('K005','사진추천');

INSERT INTO favorites (user_id, walk_id) VALUES
('user01','K001'),('user01','K002'),('user01','K003'),
('user02','K001'),('user02','K004'),('user03','K001');

INSERT INTO reviews (walk_id, user_id, rating, content) VALUES
('K001','user01',5,'코스가 길지만 경치가 좋았습니다.'),
('K001','user02',4,'화장실과 식수대가 있어 편리했습니다.'),
('K001','user03',5,''),
('K002','user01',5,'가볍게 걷기 좋은 코스였습니다.'),
('K003','user02',3,'길은 좋지만 안내 표지가 조금 부족했습니다.');

SET FOREIGN_KEY_CHECKS = 1;
