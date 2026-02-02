-- SQL snippets for report widgets
-- CUSTOMER_TYPE_RATIO: 고객유형별 건수 집계
-- 예: SELECT customer_type, COUNT(*) AS cnt FROM customer_events WHERE period = :period GROUP BY customer_type ORDER BY cnt DESC;

-- FOREIGN_TOP_COUNTRY: 외국인 국가별 상위 N 집계 (MySQL)
-- 예:
-- SELECT country_name, SUM(cnt) AS cnt
-- FROM foreign_customers
-- WHERE period = :period
-- GROUP BY country_name
-- ORDER BY cnt DESC
-- LIMIT :topN;

-- KPI 예시: GUEST_COUNT
-- SELECT COUNT(*) AS cnt FROM stay_events WHERE period = :period;
