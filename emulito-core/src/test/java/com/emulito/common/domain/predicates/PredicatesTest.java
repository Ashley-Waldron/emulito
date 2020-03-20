package com.emulito.common.domain.predicates;

import com.emulito.common.domain.http.HttpRequestContainer;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PredicatesTest {

    public static final String TEST_HTTP_REQUEST_CONTAINER_VALUE = "TestHttpRequestContainerValue";
    public static final HttpRequestContainer TEST_HTTP_REQUEST_CONTAINER = new HttpRequestContainer();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void matchesPredicateMatchFound() {
        assertThat(Predicates.matchesPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, "([A-za-z]*)").test(TEST_HTTP_REQUEST_CONTAINER), is(true));
    }

    @Test
    public void matchesPredicateMatchNotFound() {
        assertThat(Predicates.matchesPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, "([0-9]*)").test(TEST_HTTP_REQUEST_CONTAINER), is(false));
    }

    @Test
    public void matchesPredicateValueExtractedNull() {
        assertThat(Predicates.matchesPredicate(source -> null, "([A-za-z]*)").test(TEST_HTTP_REQUEST_CONTAINER), is(false));
    }

    @Test
    public void matchesPredicateValueExtractedAndRequiredValueNull() {
        assertThat(Predicates.matchesPredicate(source -> null, null).test(TEST_HTTP_REQUEST_CONTAINER), is(true));
    }

    @Test
    public void matchesPredicateRequiredValueNullValueExtractedNotNull() {
        assertThat(Predicates.matchesPredicate(source -> null, "([A-za-z]*)").test(TEST_HTTP_REQUEST_CONTAINER), is(false));
    }

    @Test
    public void startsWithPredicateMatchFound() {
        assertThat(Predicates.startsWithPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, "Test").test(TEST_HTTP_REQUEST_CONTAINER), is(true));
    }

    @Test
    public void startsWithPredicateMatchNotFound() {
        assertThat(Predicates.startsWithPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, "Value").test(TEST_HTTP_REQUEST_CONTAINER), is(false));
    }

    @Test
    public void endsWithPredicateMatchFound() {
        assertThat(Predicates.endsWithPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, "Value").test(TEST_HTTP_REQUEST_CONTAINER), is(true));
    }

    @Test
    public void endsWithPredicateMatchNotFound() {
        assertThat(Predicates.endsWithPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, "Test").test(TEST_HTTP_REQUEST_CONTAINER), is(false));
    }

    @Test
    public void containsPredicateMatchFound() {
        assertThat(Predicates.containsPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, "Req").test(TEST_HTTP_REQUEST_CONTAINER), is(true));
    }

    @Test
    public void containsPredicateMatchNotFound() {
        assertThat(Predicates.containsPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, "NoMatch").test(TEST_HTTP_REQUEST_CONTAINER), is(false));
    }

    @Test
    public void equalsPredicateMatchFound() {
        assertThat(Predicates.equalsPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, TEST_HTTP_REQUEST_CONTAINER_VALUE).test(TEST_HTTP_REQUEST_CONTAINER), is(true));
    }

    @Test
    public void equalsPredicateMatchNotFound() {
        assertThat(Predicates.equalsPredicate(source -> TEST_HTTP_REQUEST_CONTAINER_VALUE, TEST_HTTP_REQUEST_CONTAINER_VALUE.toUpperCase()).test(TEST_HTTP_REQUEST_CONTAINER), is(false));
    }
}
