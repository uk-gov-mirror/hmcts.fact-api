package uk.gov.hmcts.dts.fact.util;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {OAuthClient.class})
public class AdminFunctionalTestBase extends FunctionalTestBase {

    @Autowired
    private OAuthClient authClient;

    protected String authenticatedToken;
    protected String forbiddenToken;
    protected String superAdminToken;

    @BeforeEach
    public void setUpAuthenticationTokens() {
        authenticatedToken = authClient.getToken();
        forbiddenToken = authClient.getNobodyToken();
        superAdminToken = authClient.getSuperAdminToken();
    }
}
