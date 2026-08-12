package com.example;

import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.api.ResultBuildStrategy;
import br.com.jdbcpp.api.ResultBuildStrategyType;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReadMethodInfoFactoryTest {

    @Query("SELECT name FROM users WHERE id = :id:")
    String findNameById(Long id);

    @Query("SELECT age FROM users WHERE id = :id:")
    Integer findAgeById(Long id);

    @Query("SELECT id FROM users WHERE name = :name:")
    Long findIdByName(String name);

    @Query("SELECT active FROM users WHERE id = :id:")
    Boolean findActiveById(Long id);

    @Query("SELECT names FROM users")
    List<String> findAllNames();

    @Query("SELECT ages FROM users")
    Set<Integer> findAllAges();

    @Query("SELECT users FROM users WHERE id = :id:")
    User findUserById(Long id);

    @Query("SELECT users FROM users WHERE name = :name:")
    List<User> findUsersByName(String name);

    @Query("SELECT users FROM users WHERE active = :active:")
    Set<User> findUsersByActive(Boolean active);

    @Query("SELECT name FROM users WHERE id = :id:")
    Optional<String> findOptionalNameById(Long id);

    @Query("SELECT users FROM users WHERE id = :id:")
    Optional<User> findOptionalUserById(Long id);

    @Query("SELECT users FROM users WHERE id = :id:")
    @ResultBuildStrategy(ResultBuildStrategyType.CONSTRUCTOR)
    User findUserByIdWithConstructor(Long id);

    @Query("SELECT users FROM users WHERE name = :name:")
    @ResultBuildStrategy(ResultBuildStrategyType.CONSTRUCTOR)
    List<User> findUsersByNameWithConstructor(String name);

    @Query("SELECT users FROM users WHERE id = :id:")
    @ResultBuildStrategy(ResultBuildStrategyType.SETTER)
    User findUserByIdWithSetter(Long id);

    @Query("SELECT users FROM users WHERE name = :name:")
    @ResultBuildStrategy(value = ResultBuildStrategyType.SETTER, collectionImplementationResult = LinkedList.class)
    List<User> findUsersByNameWithCustomList(String name);

    @Query("SELECT users FROM users WHERE id = :id:")
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR, collectionImplementationResult = LinkedHashSet.class)
    Set<User> findUsersByIdWithCustomSet(Long id);

    @Query("SELECT name FROM users WHERE id = :id:")
    @ResultBuildStrategy(value = ResultBuildStrategyType.SIMPLE_RESULT, collectionImplementationResult = LinkedList.class)
    List<String> findNamesWithCustomList(Long id);

    @Query("SELECT users FROM users WHERE name = :name:")
    @ResultBuildStrategy(value = ResultBuildStrategyType.SETTER, collectionImplementationResult = LinkedList.class)
    List<User> findUsersByNameWithSetterAndCustomList(String name);

    @Query("SELECT users FROM users WHERE name = :name:")
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR, collectionImplementationResult = LinkedList.class)
    List<User> findUsersByNameWithConstructorAndCustomList(String name);

    @Query("SELECT name FROM users WHERE id = :id:")
    void invalidVoidReturn(Long id);

    record User(Long id, String name) {}

    static class UserEntity {
        private Long id;
        private String name;
    }

    class CustomException extends RuntimeException {
        public CustomException(Throwable cause) {
            super(cause);
        }
    }
}
