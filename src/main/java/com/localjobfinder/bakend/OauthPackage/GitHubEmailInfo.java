package com.localjobfinder.bakend.OauthPackage;

import lombok.Data;

@Data
public class GitHubEmailInfo {
    private String email;
    private boolean verified;

    public GitHubEmailInfo(String email, boolean verified) {
        this.email = email;
        this.verified = verified;
    }

}
