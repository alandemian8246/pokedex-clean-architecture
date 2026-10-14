package com.pokedex.application.port;

import com.pokedex.domain.model.AuthToken;
import com.pokedex.domain.model.User;

public interface TokenIssuer {

    AuthToken issue(User user);
}
