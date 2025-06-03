package br.cesar.school.linksentinel.service.observer;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;

public interface LinkStatusObserver {

    /**
     * 
     *
     * @param link 
     * @param oldResult 
     * @param newResult 
     */
    void onStatusChange(Link link, CheckResult oldResult, CheckResult newResult);
}