USE aicloud;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS ai_role_dept (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_dept (tenant_id, role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO ai_role_dept(id, tenant_id, role_id, dept_id) VALUES
(1, 1, 1, 1),
(2, 1, 2, 1),
(3, 1, 2, 2),
(4, 1, 2, 3)
ON DUPLICATE KEY UPDATE dept_id=VALUES(dept_id);
