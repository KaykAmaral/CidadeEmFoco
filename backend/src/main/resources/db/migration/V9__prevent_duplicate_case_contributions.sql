-- Remove contribuicoes do proprio criador do caso que tenham sido agrupadas
-- antes da regra de contribuicao unica.
DELETE report
FROM occurrences report
JOIN occurrences case_root ON case_root.id = report.group_root_id
WHERE report.user_id = case_root.user_id;

-- Mantem somente o primeiro relato de cada cidadao em cada caso.
DELETE duplicate_report
FROM occurrences duplicate_report
JOIN occurrences first_report
  ON first_report.group_root_id = duplicate_report.group_root_id
 AND first_report.user_id = duplicate_report.user_id
 AND first_report.id < duplicate_report.id
WHERE duplicate_report.group_root_id IS NOT NULL;

-- Usa uma tabela temporaria porque o MySQL nao permite atualizar uma tabela
-- enquanto uma subconsulta correlacionada le a mesma tabela. A chave primaria
-- explicita tambem atende provedores que habilitam sql_require_primary_key.
CREATE TEMPORARY TABLE occurrence_strength_totals (
    group_root_id BIGINT NOT NULL,
    report_count BIGINT NOT NULL,
    PRIMARY KEY (group_root_id)
);

INSERT INTO occurrence_strength_totals (group_root_id, report_count)
SELECT group_root_id, COUNT(*) AS report_count
FROM occurrences
WHERE group_root_id IS NOT NULL
GROUP BY group_root_id;

UPDATE occurrences case_root
LEFT JOIN occurrence_strength_totals totals ON totals.group_root_id = case_root.id
SET case_root.strength = 1 + COALESCE(totals.report_count, 0)
WHERE case_root.group_root_id IS NULL;

DROP TEMPORARY TABLE occurrence_strength_totals;

ALTER TABLE occurrences
    ADD CONSTRAINT uk_occurrences_group_user UNIQUE (group_root_id, user_id);
