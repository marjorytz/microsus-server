package service;

public class GerenciadorAuth {
    private static final String usuario_fixo = "admin";
    private static final String senha_fixa = "admin123";
    private static final String token = "a1b2c3d4e5f6";

    public String autenticar(String usuario, String senha) {
        if (usuario_fixo.equals(usuario) && senha_fixa.equals(senha)) {
            return token;
        }
        return null;
    }

    public boolean validarToken(String tokenRecebido) {
        if(tokenRecebido == null) {
            return false;
        }
        return token.equals(tokenRecebido);
    }
}
