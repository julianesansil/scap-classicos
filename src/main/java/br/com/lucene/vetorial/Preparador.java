package br.com.lucene.vetorial;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;

import br.com.lucene.Util;

public class Preparador {
	private static Logger logger = Logger.getLogger(Preparador.class);

	// Diret�rio de armazenamento da base preparada
	private File dirBasePreparada;

	// Biblioteca que extrai texto de diversos formatos conhecidos
	private Tika tika;

	public Preparador(File dirBasePreparada) {
		this.dirBasePreparada = dirBasePreparada;
	}

	public Tika getTika() {
		if (tika == null) {
			tika = new Tika();
		}
		return tika;
	}

	// Concatena, em arquivos, os n-grams separados por autor
	public void prepararDiretorio(File dirBase, String prefixoAceito, String sufixoAceito, File arquivoParaRetirar) {
		// Associa o autor com a sua respectiva lista de n-grams
		Map<String, String> nGramsPorAutor = recuperarNGramsPorAutor(dirBase, prefixoAceito, sufixoAceito, arquivoParaRetirar);

		// Salva, em arquivos nomeados com o nome do autor, os n-grams do autor
		for (String autor : nGramsPorAutor.keySet()) {
			String listNGramsAutor = nGramsPorAutor.get(autor);
			String pathArquivo = dirBasePreparada + "/" + autor + sufixoAceito;

			try {
				logger.info("Salvando o arquivo (n-grams/autor): " + pathArquivo);
				Util.salvarArquivo(pathArquivo, listNGramsAutor);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	// De um conjunto de arquivos com varios n-grams, separa os n-grams por
	// autor
	public Map<String, String> recuperarNGramsPorAutor(File dirBase, String prefixoAceito, String sufixoAceito, File arquivoParaRetirar) {
		// {"autor", "listNGramsAutor"}
		Map<String, String> nGramsPorAutor = new HashMap<String, String>();

		for (File arquivo : Util.listarArquivosDiretorio(dirBase, prefixoAceito, sufixoAceito)) {
			String autor = "";
			StringBuffer listNGramsAutor = new StringBuffer();

			if (!arquivo.equals(arquivoParaRetirar)) {
				if (arquivo.isFile()) {
					//logger.info("Preparando o arquivo: " + arquivo);
					autor = Util.getNomeAutor(arquivo);

					try {
						if (nGramsPorAutor.get(autor) != null)
							listNGramsAutor.append(nGramsPorAutor.get(autor));
						// Extrai o conte�do do arquivo com o Tika
						listNGramsAutor.append(getTika().parseToString(arquivo));

						// Atualiza os n-grams do autor
						nGramsPorAutor.put(autor, listNGramsAutor.toString());

					} catch (Exception e) {
						logger.error(e);
					}
				}
			}
		}

		return nGramsPorAutor;
	}

}