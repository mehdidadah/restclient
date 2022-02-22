package fr.netfit.commons.rest.client;

import fr.netfit.commons.rest.client.request.GetRequest;
import fr.netfit.commons.rest.client.request.PostRequest;

import java.util.Optional;

public interface RestClient {

    <T> Optional<T> get(GetRequest<T> request);

    <T> Optional<T> post(PostRequest<T> request);
}
