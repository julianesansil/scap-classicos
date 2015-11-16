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

	// Diretório de armazenamento da base preparada
	private File dirBasePreparada;

	// Delimitador dos termos das strings dos índices e consulta
	private String delimitadorSeparacao;

	// Biblioteca que extrai texto de diversos formatos conhecidos
	private Tika tika;

	public Preparador(File dirBasePreparada, String delimitadorSeparacao) {
		this.dirBasePreparada = dirBasePreparada;
		this.delimitadorSeparacao = delimitadorSeparacao;
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

			// Remove os termos de 1 ocorrência da string
			listNGramsAutor = removerTermos1Ocorrencia(listNGramsAutor);

			String arquivo = dirBasePreparada + "/" + autor + sufixoAceito;

			try {
				// logger.info("Salvando o arquivo (n-grams/autor): " + arquivo);
				Util.salvarArquivo(arquivo, listNGramsAutor);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	// De um conjunto de arquivos com varios n-grams, separa os n-grams por autor
	public Map<String, String> recuperarNGramsPorAutor(File dirBase, String prefixoAceito, String sufixoAceito, File arquivoParaRetirar) {
		// {"autor", "listNGramsAutor"}
		Map<String, String> nGramsPorAutor = new HashMap<String, String>();

		for (File arquivo : Util.listarArquivosDiretorio(dirBase, prefixoAceito, sufixoAceito)) {
			String autor = Util.getNomeAutor(arquivo);
			StringBuffer listNGramsAutor = new StringBuffer();

			if (nGramsPorAutor.get(autor) == null)
				nGramsPorAutor.put(autor, listNGramsAutor.toString());
			if (!arquivo.equals(arquivoParaRetirar)) {
				if (arquivo.isFile()) {
					// logger.info("Preparando o arquivo: " + arquivo);

					try {
						listNGramsAutor.append(nGramsPorAutor.get(autor));
						// Extrai o conteúdo do arquivo com o Tika
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

	public String removerTermos1Ocorrencia(String conteudo) {
		StringBuffer conteudoSemTermos1Ocorrencia = new StringBuffer();

		Map<String, Integer> conteudoIndexado = new HashMap<String, Integer>();
		String conteudoSplit[] = conteudo.split(" ");
		int frequenciaTermo;

		for (String termo : conteudoSplit) {
			if (conteudoIndexado.get(termo) == null)
				conteudoIndexado.put(termo, 1);
			else {
				frequenciaTermo = conteudoIndexado.get(termo);
				conteudoIndexado.put(termo, ++frequenciaTermo);
			}
		}

		for (String termo : conteudoIndexado.keySet()) {
			if (conteudoIndexado.get(termo) != 1) {
				frequenciaTermo = conteudoIndexado.get(termo);

				for (int i = 0; i < frequenciaTermo; i++) {
					conteudoSemTermos1Ocorrencia.append(termo);
					conteudoSemTermos1Ocorrencia.append(delimitadorSeparacao);
				}
			}
		}

		return conteudoSemTermos1Ocorrencia.toString();
	}

}
