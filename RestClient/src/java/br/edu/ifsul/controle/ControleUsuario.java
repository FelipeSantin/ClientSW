/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsul.controle;

import br.edu.ifsul.ejb.BeanServer;
import br.edu.ifsul.ejb.BeanUsuario;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Felipe
 */
@Named(value = "controleUsuario")
@SessionScoped
public class ControleUsuario implements Serializable{
    private String msg = "", erro;
    @EJB
    private BeanUsuario beanUsuario;
    @EJB
    private BeanServer beanServer;
    
    public ControleUsuario() {
    }

    public String batepapo(){
        getMensagem();
        return "/batepapo/batepapo?faces-redirect=true";
    }
    
    public String index(){
        return "/index?faces-redirect=true";
    }
    
    public String inicia(){
        setErro("");
        boolean b = true;
        //testa se tem permissao rest
        b = testaUsuario();
        if(b){
            setErro("");
            return batepapo();
        }
        setErro("Erro ao acessar");
        return index();
    }
    
    public String salvar(){
        if(beanUsuario.getNome() != null){
            EnviaMensagem(getMsg());
            setMsg("");
            return batepapo();
        }
        else
            return index();
    }
    
    public BeanUsuario getBeanUsuario() {
        return beanUsuario;
    }

    public void setBeanUsuario(BeanUsuario beanUsuario) {
        this.beanUsuario = beanUsuario;
    }
    
    public BeanServer getBeanServer() {
        return beanServer;
    }

    public void setBeanServer(BeanServer beanServer) {
        this.beanServer = beanServer;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    private boolean testaUsuario() {
        Client client = Client.create();
        WebResource webResource = client
           .resource("http://localhost:8090/login");
        String input = "{\"username\":\"" + beanUsuario.getNome().trim() + "\",\"password\":\"" + beanUsuario.getSenha().trim()+ "\"}";
        ClientResponse response = webResource.type("application/json")
		   .post(ClientResponse.class, input);
        try {

            if (response.getStatus() == 401) {
               return false;
            }
            
            String token = response.getEntity(String.class);
            
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(token);
            
            beanUsuario.setToken(json.get("token").toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            setErro("ocorreu um erro");
            return false;
        }
    }

    private boolean EnviaMensagem(String msg) {
        try {
            Client client = Client.create();
            WebResource webResource = client
               .resource("http://localhost:8090/msg");
            String input = "{\"msg\":\"" + msg+ "\"}";
            ClientResponse response = webResource.type("application/json")
                       .header("Authorization", "tkn " + beanUsuario.getToken())
                       .post(ClientResponse.class, input);
            if (response.getStatus() == 401) {
               setErro("Usuario não possui acesso.");
            }
            getMensagem();
            setMsg("");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            setErro("ocorreu um erro");
            return false;
        }
    }
    
    public void getMensagem(){
        try {
            Client client = Client.create();

            WebResource webResource = client
               .resource("http://localhost:8090/msg");

            ClientResponse response = webResource.accept("application/json")
               .header("Authorization", "tkn " + beanUsuario.getToken())
               .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                setErro("usuario nao tem permissao");
            }

            String output = response.getEntity(String.class);
            if (!output.isEmpty()){

                JSONParser parser = new JSONParser();
                JSONArray msgs = (JSONArray) parser.parse(output);
                Iterator i = msgs.iterator();

                beanServer.setMensagens(new ArrayList<>());
                while (i.hasNext()) {
                    JSONObject msg = (JSONObject) i.next();
                    
                    String msgFinal = (String)msg.get("usuarioPostou") + " postou: " + (String)msg.get("msg");
                    beanServer.setMensagem(msgFinal);
                }
            }

        } catch (Exception e) {
            setErro("ocorreu um erro");
            e.printStackTrace();
	}
    }
    
}
