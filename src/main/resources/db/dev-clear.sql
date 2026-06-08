-- =============================================================================
-- Jet Backoffice — limpar dados do seed de desenvolvimento
-- =============================================================================
-- Remove apenas registros criados por dev-seed.sql (marcadores email/whatsapp/name).
-- Não altera schema (incl. overdue_due_advance_pending em subscriber_member). Outros dados permanecem.
--
-- Fluxo recomendado:
--   psql "postgresql://backoffice:backoffice@localhost:5432/backoffice" \
--     -f backoffice/src/main/resources/db/dev-clear.sql
--   psql "postgresql://backoffice:backoffice@localhost:5432/backoffice" \
--     -f backoffice/src/main/resources/db/dev-seed.sql
--
--   docker compose -f backoffice/docker-compose.yml exec -T postgres \
--     psql -U backoffice -d backoffice < backoffice/src/main/resources/db/dev-clear.sql
-- =============================================================================

BEGIN;

CREATE TEMP TABLE seed_users ON COMMIT DROP AS
SELECT u.id
FROM users u
WHERE u.email IN (
  'carlosmiranda19122@gmail.com',
  'sponsor_01@gmail.com', 'sponsor_02@gmail.com', 'sponsor_03@gmail.com',
  'sponsor_04@gmail.com', 'sponsor_05@gmail.com', 'sponsor_06@gmail.com',
  'sponsor_07@gmail.com', 'sponsor_08@gmail.com', 'sponsor_09@gmail.com',
  'sponsor_10@gmail.com',
  'member_subscriber_01@gmail.com', 'member_subscriber_02@gmail.com',
  'member_subscriber_03@gmail.com', 'member_subscriber_04@gmail.com',
  'member_subscriber_05@gmail.com',
  'member_sponsored_01@gmail.com', 'member_sponsored_02@gmail.com',
  'member_sponsored_03@gmail.com', 'member_sponsored_04@gmail.com',
  'member_sponsored_05@gmail.com'
);

CREATE TEMP TABLE seed_sponsors ON COMMIT DROP AS
SELECT s.id
FROM sponsors s
WHERE s.whatsapp BETWEEN '11990000001' AND '11990000010';

CREATE TEMP TABLE seed_members ON COMMIT DROP AS
SELECT m.id
FROM members m
WHERE m.whatsapp BETWEEN '11980000001' AND '11980000010';

CREATE TEMP TABLE seed_subscribers ON COMMIT DROP AS
SELECT sm.id
FROM subscriber_member sm
WHERE sm.member_id IN (SELECT id FROM seed_members);

-- 1) Eventos de cobrança (filhos de subscriber_member)
DELETE FROM subscriber_payment_event e
WHERE e.subscriber_member_id IN (SELECT id FROM seed_subscribers)
   OR e.note LIKE 'dev-seed-%';

-- 2) Patrocínio associação
DELETE FROM sponsored_member sp
WHERE sp.member_id IN (SELECT id FROM seed_members)
   OR sp.granted_by_user_id IN (SELECT id FROM seed_users);

-- 3) Assinantes
DELETE FROM subscriber_member sm
WHERE sm.member_id IN (SELECT id FROM seed_members);

-- 4) Check-in (se existir para seed)
DELETE FROM sponsors_members_checkin c
WHERE c.sponsor_id IN (SELECT id FROM seed_sponsors)
   OR c.member_id IN (SELECT id FROM seed_members);

-- 5) Benefícios do seed
DELETE FROM benefits b
WHERE b.name LIKE 'dev-seed-benefit-%'
   OR b.sponsor_id IN (SELECT id FROM seed_sponsors);

-- 6) Códigos de passe vinculados a usuários seed
DELETE FROM generate_pass_codes g
WHERE g.user_id IN (SELECT id FROM seed_users);

-- 7) Members
DELETE FROM members m
WHERE m.id IN (SELECT id FROM seed_members);

-- 8) Sponsors
DELETE FROM sponsors s
WHERE s.id IN (SELECT id FROM seed_sponsors);

-- 9) Users
DELETE FROM users u
WHERE u.id IN (SELECT id FROM seed_users);

-- Reseta sequences (vazio → próximo id 1; com dados → max+1)
SELECT setval('users_seq', COALESCE((SELECT MAX(id) FROM users), 1), EXISTS (SELECT 1 FROM users));
SELECT setval('sponsors_seq', COALESCE((SELECT MAX(id) FROM sponsors), 1), EXISTS (SELECT 1 FROM sponsors));
SELECT setval('members_seq', COALESCE((SELECT MAX(id) FROM members), 1), EXISTS (SELECT 1 FROM members));
SELECT setval('subscriber_member_seq', COALESCE((SELECT MAX(id) FROM subscriber_member), 1), EXISTS (SELECT 1 FROM subscriber_member));
SELECT setval('subscriber_payment_event_seq', COALESCE((SELECT MAX(id) FROM subscriber_payment_event), 1), EXISTS (SELECT 1 FROM subscriber_payment_event));
SELECT setval('benefits_seq', COALESCE((SELECT MAX(id) FROM benefits), 1), EXISTS (SELECT 1 FROM benefits));
SELECT setval('sponsors_members_checkin_seq', COALESCE((SELECT MAX(id) FROM sponsors_members_checkin), 1), EXISTS (SELECT 1 FROM sponsors_members_checkin));

COMMIT;
