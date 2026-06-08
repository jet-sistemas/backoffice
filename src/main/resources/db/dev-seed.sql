-- =============================================================================
-- Jet Backoffice — seed de desenvolvimento (execução manual, uma vez)
-- =============================================================================
-- Pré-requisito: schema criado pelo Quarkus (hibernate-orm.database.generation=update)
--
-- Repopular do zero (limpar seed antes):
--   psql "postgresql://backoffice:backoffice@localhost:5432/backoffice" \
--     -f backoffice/src/main/resources/db/dev-clear.sql
--   psql "postgresql://backoffice:backoffice@localhost:5432/backoffice" \
--     -f backoffice/src/main/resources/db/dev-seed.sql
--
-- Só popular (idempotente):
--   psql "postgresql://backoffice:backoffice@localhost:5432/backoffice" \
--     -f backoffice/src/main/resources/db/dev-seed.sql
--
-- docker compose exec -T postgres psql -U backoffice -d backoffice < src/main/resources/db/dev-clear.sql
-- docker compose exec -T postgres psql -U backoffice -d backoffice < src/main/resources/db/dev-seed.sql
--
-- Idempotente: reexecutar não duplica (guards por email / whatsapp / nota seed).
-- Dados: 21 users, 10 sponsors, 100 benefits (10/sponsor), 10 members, billing, eventos.
-- subscriber_member inclui overdue_due_advance_pending (F3 — ciclo OVERDUE em duas etapas).
-- Senha de todos os usuários: mesmo plaintext usado para gerar o hash abaixo.
-- =============================================================================

BEGIN;

-- Sincroniza sequences com IDs já criados pelo Hibernate
SELECT setval('users_seq', COALESCE((SELECT MAX(id) FROM users), 1));
SELECT setval('sponsors_seq', COALESCE((SELECT MAX(id) FROM sponsors), 1));
SELECT setval('members_seq', COALESCE((SELECT MAX(id) FROM members), 1));
SELECT setval('subscriber_member_seq', COALESCE((SELECT MAX(id) FROM subscriber_member), 1));
SELECT setval('subscriber_payment_event_seq', COALESCE((SELECT MAX(id) FROM subscriber_payment_event), 1));
SELECT setval('benefits_seq', COALESCE((SELECT MAX(id) FROM benefits), 1));

-- Parâmetros de datas relativas (due_soon_days = 5)
CREATE TEMP TABLE IF NOT EXISTS seed_dates (
  today date PRIMARY KEY,
  due_soon_end date NOT NULL,
  sub01_next_due date NOT NULL,
  sub02_next_due date NOT NULL,
  sub03_next_due date NOT NULL,
  sub04_next_due date NOT NULL,
  sub05_next_due date NOT NULL
);

DELETE FROM seed_dates;
INSERT INTO seed_dates (today, due_soon_end, sub01_next_due, sub02_next_due, sub03_next_due, sub04_next_due, sub05_next_due)
SELECT
  CURRENT_DATE,
  CURRENT_DATE + 5,
  -- ACTIVE: vencimento > today + 5 (billing_day 10)
  CASE
    WHEN (date_trunc('month', CURRENT_DATE)::date + 9) > CURRENT_DATE + 5
      THEN date_trunc('month', CURRENT_DATE)::date + 9
    ELSE (date_trunc('month', CURRENT_DATE) + interval '1 month')::date + 9
  END,
  -- DUE_SOON: vencimento em [today, today+5]
  LEAST(CURRENT_DATE + 3, CURRENT_DATE + 5),
  -- OVERDUE: vencimento < today
  CURRENT_DATE - 10,
  -- ACTIVE com histórico: vencimento futuro após simulação de pagamento
  CASE
    WHEN (date_trunc('month', CURRENT_DATE)::date + 19) > CURRENT_DATE + 5
      THEN date_trunc('month', CURRENT_DATE)::date + 19
    ELSE (date_trunc('month', CURRENT_DATE) + interval '1 month')::date + 19
  END,
  -- INACTIVE: valor placeholder
  CURRENT_DATE + 10;

-- -----------------------------------------------------------------------------
-- 1) USERS (21)
-- -----------------------------------------------------------------------------
INSERT INTO users (id, email, password, name, document, code, is_account_active, type, created_at, updated_at)
SELECT nextval('users_seq'), v.email, '$2a$12$AUBavi4Bm4tmciYwUKvK3O.RRvOx3LlSAe.fT8p3OE/ZLa8yEYU/q', v.name, v.document, v.code, v.is_active, v.type, NOW(), NOW()
FROM (VALUES
  ('carlosmiranda19122@gmail.com', 'Carlos Augusto', '06270297362', 'CADM1', true,  'ADM'),
  ('sponsor_01@gmail.com', 'Patrocinador 01', '10000000001', 'SP001', true,  'SPONSOR'),
  ('sponsor_02@gmail.com', 'Patrocinador 02', '10000000002', 'SP002', true,  'SPONSOR'),
  ('sponsor_03@gmail.com', 'Patrocinador 03', '10000000003', 'SP003', true,  'SPONSOR'),
  ('sponsor_04@gmail.com', 'Patrocinador 04', '10000000004', 'SP004', false, 'SPONSOR'),
  ('sponsor_05@gmail.com', 'Patrocinador 05', '10000000005', 'SP005', false, 'SPONSOR'),
  ('sponsor_06@gmail.com', 'Patrocinador 06', '10000000006', 'SP006', true,  'SPONSOR'),
  ('sponsor_07@gmail.com', 'Patrocinador 07', '10000000007', 'SP007', true,  'SPONSOR'),
  ('sponsor_08@gmail.com', 'Patrocinador 08', '10000000008', 'SP008', false, 'SPONSOR'),
  ('sponsor_09@gmail.com', 'Patrocinador 09', '10000000009', 'SP009', false, 'SPONSOR'),
  ('sponsor_10@gmail.com', 'Patrocinador 10', '10000000010', 'SP010', false, 'SPONSOR'),
  ('member_subscriber_01@gmail.com', 'Assinante 01', '20000000001', 'SU001', true,  'MEMBER'),
  ('member_subscriber_02@gmail.com', 'Assinante 02', '20000000002', 'SU002', true,  'MEMBER'),
  ('member_subscriber_03@gmail.com', 'Assinante 03', '20000000003', 'SU003', true,  'MEMBER'),
  ('member_subscriber_04@gmail.com', 'Assinante 04', '20000000004', 'SU004', true,  'MEMBER'),
  ('member_subscriber_05@gmail.com', 'Assinante 05', '20000000005', 'SU005', false, 'MEMBER'),
  ('member_sponsored_01@gmail.com', 'Patrocinado 01', '20000000006', 'SD001', true,  'MEMBER'),
  ('member_sponsored_02@gmail.com', 'Patrocinado 02', '20000000007', 'SD002', false, 'MEMBER'),
  ('member_sponsored_03@gmail.com', 'Patrocinado 03', '20000000008', 'SD003', false, 'MEMBER'),
  ('member_sponsored_04@gmail.com', 'Patrocinado 04', '20000000009', 'SD004', false, 'MEMBER'),
  ('member_sponsored_05@gmail.com', 'Patrocinado 05', '20000000010', 'SD005', false, 'MEMBER')
) AS v(email, name, document, code, is_active, type)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = v.email);

-- -----------------------------------------------------------------------------
-- 2) SPONSORS (10) — tier + entity_type distribuídos
-- -----------------------------------------------------------------------------
INSERT INTO sponsors (id, user_id, public_name, tier, entity_type, persona, whatsapp, is_active, last_active_sponsorship, created_at, updated_at)
SELECT nextval('sponsors_seq'), u.id, v.public_name, v.tier, v.entity_type, v.persona, v.whatsapp, v.is_active,
  CASE WHEN v.is_active THEN NULL ELSE NOW() END,
  NOW(), NOW()
FROM (VALUES
  ('sponsor_01@gmail.com', 'Patrocínio Bronze Pessoa', 'BRONZE', 'PERSON', 'POLITICIAN', '11990000001', true),
  ('sponsor_02@gmail.com', 'Patrocínio Bronze Empresa', 'BRONZE', 'COMPANY', NULL, '11990000002', true),
  ('sponsor_03@gmail.com', 'Patrocínio Prata Pessoa', 'SILVER', 'PERSON', 'INFLUENCER', '11990000003', true),
  ('sponsor_04@gmail.com', 'Patrocínio Ouro ONG', 'GOLD', 'NGO', NULL, '11990000004', false),
  ('sponsor_05@gmail.com', 'Patrocínio Bronze Governo', 'BRONZE', 'GOVERNMENT', NULL, '11990000005', false),
  ('sponsor_06@gmail.com', 'Patrocínio Prata Empresa', 'SILVER', 'COMPANY', NULL, '11990000006', true),
  ('sponsor_07@gmail.com', 'Patrocínio Ouro Atleta', 'GOLD', 'PERSON', 'ATHLETE', '11990000007', true),
  ('sponsor_08@gmail.com', 'Patrocínio Bronze ONG 2', 'BRONZE', 'NGO', NULL, '11990000008', false),
  ('sponsor_09@gmail.com', 'Patrocínio Prata Governo', 'SILVER', 'GOVERNMENT', NULL, '11990000009', false),
  ('sponsor_10@gmail.com', 'Patrocínio Ouro Outros', 'GOLD', 'PERSON', 'OTHER', '11990000010', false)
) AS v(email, public_name, tier, entity_type, persona, whatsapp, is_active)
JOIN users u ON u.email = v.email
WHERE NOT EXISTS (SELECT 1 FROM sponsors s WHERE s.whatsapp = v.whatsapp);

-- -----------------------------------------------------------------------------
-- 2b) BENEFITS (100 — 10 por sponsor)
-- -----------------------------------------------------------------------------
INSERT INTO benefits (id, sponsor_id, name, description, address, is_active, created_at, updated_at)
SELECT
  nextval('benefits_seq'),
  s.id,
  'dev-seed-benefit-' || s.whatsapp || '-' || lpad(n::text, 2, '0'),
  'Benefício seed ' || n || ' — ' || s.tier || ' / ' || s.entity_type,
  'Rua Seed ' || n || ', 100 — Patrocinador ' || s.public_name,
  s.is_active,
  NOW(), NOW()
FROM sponsors s
CROSS JOIN generate_series(1, 10) AS n
WHERE s.whatsapp BETWEEN '11990000001' AND '11990000010'
  AND NOT EXISTS (
    SELECT 1 FROM benefits b
    WHERE b.sponsor_id = s.id
      AND b.name = 'dev-seed-benefit-' || s.whatsapp || '-' || lpad(n::text, 2, '0')
  );

-- -----------------------------------------------------------------------------
-- 3) MEMBERS (10)
-- -----------------------------------------------------------------------------
INSERT INTO members (id, user_id, fullname, whatsapp, type, is_active, created_at, updated_at)
SELECT nextval('members_seq'), u.id, v.fullname, v.whatsapp, v.type, v.is_active, NOW(), NOW()
FROM (VALUES
  ('member_subscriber_01@gmail.com', 'Assinante 01', '11980000001', 'SUBSCRIBER', true),
  ('member_subscriber_02@gmail.com', 'Assinante 02', '11980000002', 'SUBSCRIBER', true),
  ('member_subscriber_03@gmail.com', 'Assinante 03', '11980000003', 'SUBSCRIBER', true),
  ('member_subscriber_04@gmail.com', 'Assinante 04', '11980000004', 'SUBSCRIBER', true),
  ('member_subscriber_05@gmail.com', 'Assinante 05', '11980000005', 'SUBSCRIBER', false),
  ('member_sponsored_01@gmail.com', 'Patrocinado 01', '11980000006', 'SPONSORED', true),
  ('member_sponsored_02@gmail.com', 'Patrocinado 02', '11980000007', 'SPONSORED', false),
  ('member_sponsored_03@gmail.com', 'Patrocinado 03', '11980000008', 'SPONSORED', false),
  ('member_sponsored_04@gmail.com', 'Patrocinado 04', '11980000009', 'SPONSORED', false),
  ('member_sponsored_05@gmail.com', 'Patrocinado 05', '11980000010', 'SPONSORED', false)
) AS v(email, fullname, whatsapp, type, is_active)
JOIN users u ON u.email = v.email
WHERE NOT EXISTS (SELECT 1 FROM members m WHERE m.whatsapp = v.whatsapp);

-- -----------------------------------------------------------------------------
-- 4) SUBSCRIBER_MEMBER (5)
-- -----------------------------------------------------------------------------
INSERT INTO subscriber_member (
  id, member_id, monthly_fee_amount, billing_day, status, next_due_date,
  last_paid_at, overdue_due_advance_pending, created_at, updated_at
)
SELECT nextval('subscriber_member_seq'), m.id, 20.00, v.billing_day, v.status, v.next_due,
  v.last_paid, v.overdue_pending, NOW(), NOW()
FROM (VALUES
  ('member_subscriber_01@gmail.com', 10, 'ACTIVE',   (SELECT sub01_next_due FROM seed_dates), NULL,  false),
  ('member_subscriber_02@gmail.com', 15, 'DUE_SOON', (SELECT sub02_next_due FROM seed_dates), NULL,  false),
  ('member_subscriber_03@gmail.com',  5, 'OVERDUE',  (SELECT sub03_next_due FROM seed_dates), NULL,  false),
  ('member_subscriber_04@gmail.com', 20, 'ACTIVE',   (SELECT sub04_next_due FROM seed_dates), NOW() - interval '5 days', false),
  ('member_subscriber_05@gmail.com', 10, 'INACTIVE', (SELECT sub05_next_due FROM seed_dates), NULL,  false)
) AS v(email, billing_day, status, next_due, last_paid, overdue_pending)
JOIN users u ON u.email = v.email
JOIN members m ON m.user_id = u.id
WHERE NOT EXISTS (SELECT 1 FROM subscriber_member sm WHERE sm.member_id = m.id);

-- Reexecução: sincroniza flag F3 nos assinantes seed já existentes
UPDATE subscriber_member sm
SET overdue_due_advance_pending = v.overdue_pending, updated_at = NOW()
FROM (VALUES
  ('member_subscriber_01@gmail.com', false),
  ('member_subscriber_02@gmail.com', false),
  ('member_subscriber_03@gmail.com', false),
  ('member_subscriber_04@gmail.com', false),
  ('member_subscriber_05@gmail.com', false)
) AS v(email, overdue_pending)
JOIN users u ON u.email = v.email
JOIN members m ON m.user_id = u.id
WHERE sm.member_id = m.id
  AND sm.overdue_due_advance_pending IS DISTINCT FROM v.overdue_pending;

-- -----------------------------------------------------------------------------
-- 5) SPONSORED_MEMBER (5) — concedente = sponsor_01 (ativo)
-- -----------------------------------------------------------------------------
INSERT INTO sponsored_member (member_id, granted_by_user_id, start_at, end_at, reason, is_active, created_at, updated_at)
SELECT m.id, grantor.id, CURRENT_DATE - 30, NULL, 'Patrocínio associação (seed dev)', true, NOW(), NOW()
FROM (VALUES
  ('member_sponsored_01@gmail.com'),
  ('member_sponsored_02@gmail.com'),
  ('member_sponsored_03@gmail.com'),
  ('member_sponsored_04@gmail.com'),
  ('member_sponsored_05@gmail.com')
) AS v(email)
JOIN users u ON u.email = v.email
JOIN members m ON m.user_id = u.id
JOIN users grantor ON grantor.email = 'sponsor_01@gmail.com'
WHERE NOT EXISTS (
  SELECT 1 FROM sponsored_member sp
  WHERE sp.member_id = m.id AND sp.granted_by_user_id = grantor.id
);

-- -----------------------------------------------------------------------------
-- 6) SUBSCRIBER_PAYMENT_EVENT (>= 3 por assinante ativo 01–04)
-- -----------------------------------------------------------------------------
-- Assinante 01
INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, adm.id, 'BILLING_CONFIG_UPDATED',
  'ACTIVE', 'ACTIVE', d.sub01_next_due, d.sub01_next_due,
  20.00, 20.00, 10, 10, NULL, 'dev-seed-sub01-evt1', NOW() - interval '90 days', NOW() - interval '90 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_01@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
JOIN users adm ON adm.email = 'carlosmiranda19122@gmail.com'
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e
  WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub01-evt1'
);

INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, adm.id, 'STATUS_AUTO_UPDATED',
  'ACTIVE', 'DUE_SOON', d.sub01_next_due, d.sub02_next_due,
  20.00, 20.00, 10, 10, NULL, 'dev-seed-sub01-evt2', NOW() - interval '30 days', NOW() - interval '30 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_01@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
JOIN users adm ON adm.email = 'carlosmiranda19122@gmail.com'
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e
  WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub01-evt2'
);

INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, adm.id, 'PAYMENT_MARKED_PAID',
  'DUE_SOON', 'ACTIVE', d.sub02_next_due, d.sub01_next_due,
  20.00, 20.00, 10, 10, 20.00, 'dev-seed-sub01-evt3', NOW() - interval '10 days', NOW() - interval '10 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_01@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
JOIN users adm ON adm.email = 'carlosmiranda19122@gmail.com'
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e
  WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub01-evt3'
);

-- Assinante 02
INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, adm.id, 'BILLING_CONFIG_UPDATED',
  'ACTIVE', 'ACTIVE', d.sub02_next_due + 30, d.sub02_next_due + 30,
  20.00, 20.00, 15, 15, NULL, 'dev-seed-sub02-evt1', NOW() - interval '60 days', NOW() - interval '60 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_02@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
JOIN users adm ON adm.email = 'carlosmiranda19122@gmail.com'
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub02-evt1'
);

INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, adm.id, 'STATUS_AUTO_UPDATED',
  'ACTIVE', 'DUE_SOON', d.sub02_next_due + 30, d.sub02_next_due,
  20.00, 20.00, 15, 15, NULL, 'dev-seed-sub02-evt2', NOW() - interval '20 days', NOW() - interval '20 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_02@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
JOIN users adm ON adm.email = 'carlosmiranda19122@gmail.com'
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub02-evt2'
);

INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, NULL, 'STATUS_AUTO_UPDATED',
  'DUE_SOON', 'DUE_SOON', d.sub02_next_due, d.sub02_next_due,
  20.00, 20.00, 15, 15, NULL, 'dev-seed-sub02-evt3', NOW() - interval '5 days', NOW() - interval '5 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_02@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub02-evt3'
);

-- Assinante 03 (OVERDUE)
INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, adm.id, 'BILLING_CONFIG_UPDATED',
  'ACTIVE', 'ACTIVE', d.sub03_next_due, d.sub03_next_due,
  20.00, 20.00, 5, 5, NULL, 'dev-seed-sub03-evt1', NOW() - interval '75 days', NOW() - interval '75 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_03@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
JOIN users adm ON adm.email = 'carlosmiranda19122@gmail.com'
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub03-evt1'
);

INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, NULL, 'STATUS_AUTO_UPDATED',
  'ACTIVE', 'DUE_SOON', d.sub03_next_due, d.sub03_next_due,
  20.00, 20.00, 5, 5, NULL, 'dev-seed-sub03-evt2', NOW() - interval '40 days', NOW() - interval '40 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_03@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub03-evt2'
);

INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, NULL, 'STATUS_AUTO_UPDATED',
  'DUE_SOON', 'OVERDUE', d.sub03_next_due, d.sub03_next_due,
  20.00, 20.00, 5, 5, NULL, 'dev-seed-sub03-evt3', NOW() - interval '15 days', NOW() - interval '15 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_03@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub03-evt3'
);

-- Assinante 04 (histórico + pagamento)
INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, adm.id, 'BILLING_CONFIG_UPDATED',
  'ACTIVE', 'ACTIVE', d.sub03_next_due, d.sub03_next_due,
  20.00, 20.00, 20, 20, NULL, 'dev-seed-sub04-evt1', NOW() - interval '80 days', NOW() - interval '80 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_04@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
JOIN users adm ON adm.email = 'carlosmiranda19122@gmail.com'
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub04-evt1'
);

INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, NULL, 'STATUS_AUTO_UPDATED',
  'ACTIVE', 'OVERDUE', d.sub03_next_due, d.sub03_next_due,
  20.00, 20.00, 20, 20, NULL, 'dev-seed-sub04-evt2', NOW() - interval '45 days', NOW() - interval '45 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_04@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub04-evt2'
);

INSERT INTO subscriber_payment_event (
  id, subscriber_member_id, admin_user_id, event_type,
  old_status, new_status, old_next_due_date, new_next_due_date,
  old_monthly_fee_amount, new_monthly_fee_amount, old_billing_day, new_billing_day,
  amount, note, created_at, updated_at
)
SELECT nextval('subscriber_payment_event_seq'), sm.id, adm.id, 'PAYMENT_MARKED_PAID',
  'OVERDUE', 'ACTIVE', d.sub03_next_due, d.sub04_next_due,
  20.00, 20.00, 20, 20, 20.00, 'dev-seed-sub04-evt3', NOW() - interval '5 days', NOW() - interval '5 days'
FROM seed_dates d
JOIN users mu ON mu.email = 'member_subscriber_04@gmail.com'
JOIN members m ON m.user_id = mu.id
JOIN subscriber_member sm ON sm.member_id = m.id
JOIN users adm ON adm.email = 'carlosmiranda19122@gmail.com'
WHERE NOT EXISTS (
  SELECT 1 FROM subscriber_payment_event e WHERE e.subscriber_member_id = sm.id AND e.note = 'dev-seed-sub04-evt3'
);

-- Ajuste fino: sponsor_09 conta inativa (5 sponsors ativos no total)
UPDATE users SET is_account_active = false WHERE email = 'sponsor_09@gmail.com';
UPDATE sponsors
SET is_active = false, last_active_sponsorship = COALESCE(last_active_sponsorship, NOW())
WHERE whatsapp = '11990000009';

-- Benefícios seed espelham is_active do sponsor (inclui reexecução / ajustes acima)
UPDATE benefits b
SET is_active = s.is_active
FROM sponsors s
WHERE b.sponsor_id = s.id
  AND b.name LIKE 'dev-seed-benefit-%'
  AND b.is_active IS DISTINCT FROM s.is_active;

-- Garante hash de senha no ADM pré-existente
UPDATE users
SET password = '$2a$12$AUBavi4Bm4tmciYwUKvK3O.RRvOx3LlSAe.fT8p3OE/ZLa8yEYU/q',
    name = 'Carlos Augusto',
    document = '06270297362',
    code = 'CADM1',
    is_account_active = true,
    type = 'ADM'
WHERE email = 'carlosmiranda19122@gmail.com';

SELECT setval('users_seq', COALESCE((SELECT MAX(id) FROM users), 1));
SELECT setval('sponsors_seq', COALESCE((SELECT MAX(id) FROM sponsors), 1));
SELECT setval('members_seq', COALESCE((SELECT MAX(id) FROM members), 1));
SELECT setval('subscriber_member_seq', COALESCE((SELECT MAX(id) FROM subscriber_member), 1));
SELECT setval('subscriber_payment_event_seq', COALESCE((SELECT MAX(id) FROM subscriber_payment_event), 1));
SELECT setval('benefits_seq', COALESCE((SELECT MAX(id) FROM benefits), 1));

-- Verificação (opcional):
-- SELECT s.whatsapp, COUNT(b.id) FROM sponsors s
--   LEFT JOIN benefits b ON b.sponsor_id = s.id AND b.name LIKE 'dev-seed-benefit-%'
--   WHERE s.whatsapp BETWEEN '11990000001' AND '11990000010' GROUP BY 1 ORDER BY 1;

DROP TABLE IF EXISTS seed_dates;

COMMIT;
