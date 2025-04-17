package com.example.TES.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdrRepository {

    private final JdbcTemplate jdbcTemplate;

    public Integer findStreetId(String streetName, String postalCode) {
        return jdbcTemplate.query(
                "SELECT StreetID FROM Street WHERE Street = ? AND PostalID = ?",
                rs -> rs.next() ? rs.getInt("StreetID") : null,
                streetName, postalCode
        );
    }

    public Integer insertStreet(String streetName, String postalCode) {
        jdbcTemplate.update("INSERT INTO Street (Street, PostalID) VALUES (?, ?)", streetName, postalCode);
        return jdbcTemplate.queryForObject("SELECT SCOPE_IDENTITY()", Integer.class);
    }

    public Integer insertAdr(Integer streetId, String city, String houseNo) {
        jdbcTemplate.update("INSERT INTO Adr (StreetID, Name, HouseNo) VALUES (?, ?, ?)", streetId, city, houseNo);
        return jdbcTemplate.queryForObject("SELECT SCOPE_IDENTITY()", Integer.class);
    }
}
