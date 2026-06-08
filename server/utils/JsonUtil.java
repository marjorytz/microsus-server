package utils;

public class JsonUtil {

    public static String extrairCampo(String json, String campo) {
        if (json == null || !json.contains("\"" + campo + "\"")) {
            return "";
        }
        try {
            int indexChave = json.indexOf("\"" + campo + "\"");
            int indexDoisPontos = json.indexOf(":", indexChave);
            int indexInicioAspas = json.indexOf("\"", indexDoisPontos);
            int indexFimAspas = json.indexOf("\"", indexInicioAspas + 1);
            return json.substring(indexInicioAspas + 1, indexFimAspas);
        } catch (Exception e) {
            return "";
        }
    }
}