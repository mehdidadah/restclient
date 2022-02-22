package fr.netfit.commons.rest.client.health;


import fr.netfit.commons.rest.client.response.Response;

public interface StatusProvider {

    Response<String> getStatus();

}
