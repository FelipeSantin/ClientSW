/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsul.ejb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.Singleton;

/**
 *
 * @author Felipe
 */
@Singleton
public class BeanServer {
    private List<String> mensagens = new ArrayList<>();

    public BeanServer(){
    }

    public List<String> getMensagens() {
        return mensagens;
    }

    public void setMensagens(List<String> mensagens) {
        this.mensagens = mensagens;
    }
    public void setMensagem(String msg){
        this.mensagens.add(msg);
    }   
}
