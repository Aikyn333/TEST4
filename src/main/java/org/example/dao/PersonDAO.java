package org.example.dao;

import org.example.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PersonDAO {
    @Autowired
    public PersonDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    private final JdbcTemplate jdbcTemplate;

    public List<Person> index() {

        return jdbcTemplate.query("select * from person", new BeanPropertyRowMapper<>(Person.class));
    }

    public Person show(int id) {
        return jdbcTemplate.query("select * from person where id=?", new Object[]{id}, new BeanPropertyRowMapper<>(Person.class))
                .stream()
                .findAny()
                .orElse(null);
    }
    public Optional<Person> show(String email) {
        return jdbcTemplate.query("select * from person where email=?", new Object[] {email},
                new BeanPropertyRowMapper<>(Person.class)).stream()
                .findAny();
    }

    public void save(Person person) {
        jdbcTemplate.update("insert into person(name, age, email, address) values(?,?,?,?)", person.getName(), person.getAge(), person.getEmail(), person.getAddress());
    }

    public void update(int id, Person updatedPerson) {
        jdbcTemplate.update("update person set name=?, age=?, email=?, address=? where id=?", updatedPerson.getName(), updatedPerson.getAge(), updatedPerson.getEmail(), updatedPerson.getAddress(), id);
    }

    public void delete(int id){
        jdbcTemplate.update("delete from person where id=?", id);
    }


    public void testMultipleInsert(){
        List<Person> people = create1000People();

        long start = System.currentTimeMillis();
        for(Person person : people){
            save(person);
        }
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start));
    }

    public void testBatchInsert(){
        List<Person> people = create1000People();
        long start = System.currentTimeMillis();
        jdbcTemplate.batchUpdate("insert into Person values (?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, people.get(i).getId());
                        ps.setString(2, people.get(i).getName());
                        ps.setInt(3, people.get(i).getAge());
                        ps.setString(4, people.get(i).getEmail());
                    }

                    @Override
                    public int getBatchSize() {
                        return people.size();
                    }
                });
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start));
    }
    private List<Person> create1000People(){
        List<Person> people = new ArrayList<>();
        for(int i = 0; i < 1000; i++){
            Person person = new Person(i, "name" + i, 20, "test" + i +"@mail.ru", "someAddress");
            people.add(person);
        }
        return people;
    }

}
