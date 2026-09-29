CREATE TABLE products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  business_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(1000),
  price DECIMAL(10,2) NOT NULL,
  image VARCHAR(500)
);
CREATE INDEX idx_products_business_id ON products(business_id);
