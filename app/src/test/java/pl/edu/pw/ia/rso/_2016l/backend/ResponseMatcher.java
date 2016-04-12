package pl.edu.pw.ia.rso._2016l.backend;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.Response;

public class ResponseMatcher {

    public static Matcher<Response> statusIs(final HttpStatus httpStatus) {
        return new TypeSafeMatcher<Response>() {
            @Override
            protected boolean matchesSafely(Response item) {
                return (item != null) && (item.getStatus() == httpStatus.value());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("should has status ").appendValue(httpStatus);
            }
        };
    }

}
