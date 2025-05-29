package br.cesar.school.linksentinel.service.observer;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;

public interface LinkStatusObserver {

    /**
     * Método chamado quando uma mudança significativa no status de um link monitorado é detectada.
     *
     * @param link O link que teve seu status alterado.
     * @param oldResult O resultado da verificação anterior (pode ser null se não houver histórico relevante).
     * @param newResult O novo resultado da verificação que causou a notificação.
     */
    void onStatusChange(Link link, CheckResult oldResult, CheckResult newResult);
}