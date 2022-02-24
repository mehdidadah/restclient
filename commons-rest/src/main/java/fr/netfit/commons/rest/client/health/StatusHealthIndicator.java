package fr.netfit.commons.rest.client.health;

import fr.netfit.commons.rest.client.response.Response;
import fr.netfit.commons.service.error.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.lang.NonNull;

import static javax.servlet.http.HttpServletResponse.SC_OK;

@Slf4j
public record StatusHealthIndicator(
    @NonNull StatusProvider client) implements HealthIndicator {

    @Override
    public Health health() {
        Health.Builder health;

        try {
            Response<String> response = client.getStatus();

            if (response.getStatus() == SC_OK) {
                health = Health.up();
            } else {
                health = Health.down()
                        .withDetail("http.status", response.getStatus())
                        .withDetail("http.body", response.getBody());
            }

        } catch (ServiceException e) {
            log.debug("Une erreur est survenue lors du health check", e);

            health = Health.down()
                    .withDetail("error.code", e.getError().getResponseStatus().value())
                    .withDetail("error.message", e.getMessage());

            if (e.getCause() != null) {
                health.withDetail("error.cause", e.getCause().getClass().getName() + " : " + e.getCause().getMessage());
            }

        } catch (Exception e) {
            log.error("Une erreur est survenue lors du health check", e);
            health = Health.down(e);
        }

        return health.build();
    }
}
