package com.example.TES.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AliasRepository {

    private final JdbcTemplate jdbcTemplate;

    public Integer findAdrIdByAlias(String custId, String alias) {
        String sql = "SELECT A.AdrID FROM Alias A " +
                "JOIN Adr AD ON A.AdrID = AD.AdrID " +
                "JOIN Street S ON AD.StreetID = S.StreetID " +
                "WHERE A.CustID = ? AND A.Alias = ?";

        return jdbcTemplate.query(sql, rs -> rs.next() ? rs.getInt("AdrID") : null, custId, alias);
    }

    public void insertAlias(String custId, String alias, Integer adrId, String attentionName) {
        String sql = "INSERT INTO Alias (CustID, Alias, AdrID, AttentionName, SessionDcreate) " +
                "VALUES (?, ?, ?, ?, GETDATE())";
        jdbcTemplate.update(sql, custId, alias, adrId, attentionName);
    }
}