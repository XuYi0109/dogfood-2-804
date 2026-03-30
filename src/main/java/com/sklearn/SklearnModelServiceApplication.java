package com.sklearn;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class SklearnModelServiceApplication implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {
        System.out.println("Scikit-learn Model Service starting...");
        Quarkus.waitForExit();
        return 0;
    }
}
