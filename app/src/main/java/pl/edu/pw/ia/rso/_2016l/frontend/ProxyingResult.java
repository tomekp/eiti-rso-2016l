package pl.edu.pw.ia.rso._2016l.frontend;

import lombok.ToString;
import lombok.Value;

@Value
@ToString(exclude = "details")
public final class ProxyingResult {

    String server;
    boolean successful;
    String summary;
    String details;

}
