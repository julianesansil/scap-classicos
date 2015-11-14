package br.com.lucene;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

public class Util {

	public static String getNomeAutor(File arquivo) {
		return arquivo.getName().substring(0, 2);
	}

	public static void salvarArquivo(String arquivo, String conteudo) throws IOException {
		FileWriter writer = new FileWriter(new File(arquivo));
		PrintWriter saida = new PrintWriter(writer);
		saida.println(conteudo);
		saida.close();
		writer.close();
	}

	public static File[] listarArquivosDiretorio(File diretorio, final String PREFIXO_ACEITO, final String SUFIXO_ACEITO) {
		FilenameFilter filtro = new FilenameFilter() {
			public boolean accept(File arquivo, String nome) {
				if (nome.toLowerCase().startsWith(PREFIXO_ACEITO) && nome.toLowerCase().endsWith(SUFIXO_ACEITO)) {
					return true;
				}
				return false;
			}
		};

		return diretorio.listFiles(filtro);
	}

	public static void esvaziarDiretorio(File diretorio) {
		if (diretorio.exists()) {
			File arquivos[] = diretorio.listFiles();
			for (File arquivo : arquivos) {
				arquivo.delete();
			}
		}
	}

}
