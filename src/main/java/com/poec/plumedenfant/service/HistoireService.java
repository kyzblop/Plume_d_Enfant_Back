package com.poec.plumedenfant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poec.plumedenfant.dao.HistoireDao;
import com.poec.plumedenfant.dao.model.CategorieAge;
import com.poec.plumedenfant.dao.model.CategorieHistoire;
import com.poec.plumedenfant.dao.model.FormulaireHistoire;
import com.poec.plumedenfant.dao.model.Histoire;


@Service
public class HistoireService {

	@Autowired
	private HistoireDao histoireDao;
	
	@Autowired
	private IAService iaService;
	
	@Autowired
	private UtilisateurService utilisateurService;
	
	// Création de la requete pour le corps de l'histoire
	public String creationRequete(FormulaireHistoire formulaire) {
		return "Ecris moi une histoire sur le thème " + formulaire.getCategorieHistoire().getValeur() +
				" pour un enfant de " + formulaire.getCategorieAge().getValeur() +
				". Le personnage principal s'appelle " + formulaire.getNomPersoPrincipal() + ". " + 
				formulaire.getDetailPersoPrincipal() +
				formulaire.getPhraseListePersoSecondaire() + 
				formulaire.getPhraseDetailsSupplementaires() + 
				". Donne un titre à cette histoire sur la première ligne sans utiliser de décoration de texte"
				;
		}
	
	// Creation de la requete pour l'image de l'histoire
	public String creationRequeteImage(String histoire) {
		return "Génère moi une image pour illustrer l'histoire suivante : " + histoire;
	}
	
	
	// Creation de l'histoire
	public void insertHistoire(String request, int idCreateur, CategorieHistoire categorieHistorie, CategorieAge categorieAge) {
		Histoire histoire = new Histoire();
		histoire.setId(null);
		histoire.setCategorieHistoire(categorieHistorie);
		histoire.setCategorieAge(categorieAge);
		utilisateurService.getUtilisateurById(idCreateur).ifPresent(utilisateur -> {
			histoire.setCreateur(utilisateur);
		});
		
		System.out.println(request);
		String contenuHistoire = iaService.faireRequete(request);
		
		// Extraction du titre pour le mettre dans l'attribut titre
		int finTitre = contenuHistoire.indexOf("\n");
		String titre = contenuHistoire.substring(0, finTitre);
		contenuHistoire = contenuHistoire.substring(finTitre + 2); 
		histoire.setTitre(titre);
		
		// On met le corps de l'histoire sans le titre
		if(!contenuHistoire.equals("error")) {
			histoire.setCorps(contenuHistoire);
		}
		
		// Récupération de l'image en B64Json
		String imageB64Json = iaService.creerImage(contenuHistoire);
		if(!imageB64Json.equals("error")) {
			histoire.setImageB64Json(imageB64Json);
		}
		
		histoireDao.save(histoire);
	}
	
	// Récupération d'une histoire
	public Histoire getHistoireById(int idHistoire) {
		return histoireDao.findById(idHistoire).orElse(null);
	}
	
	// Récupération de la liste d'histoire
	public List<Histoire> getAllHistoire() {
		return (List<Histoire>) histoireDao.findAll();
	}
	
	// Modification d'une histoire
	public void updateHistoire(Histoire histoire, int idHistoire) {
		if(histoireDao.findById(idHistoire) != null) {
			// update du corps de l'histoire
			if(histoire.getCorps() != null) {
				histoireDao.updateCorps(idHistoire, histoire.getCorps());
			}
			// update du nombre de like de l'histoire
			if(histoire.getNbLike() != null) {
				histoireDao.updateNbLike(idHistoire, histoire.getNbLike());
			}
			// update du titre de l'histoire
			if(histoire.getTitre() != null) {
				histoireDao.updateTitre(idHistoire, histoire.getTitre());
			}
			// update de l'image de l'histoire
			if(histoire.getImageB64Json() != null) {
				histoireDao.updateUrlImage(idHistoire, histoire.getImageB64Json());
			}
		} else {
			System.out.println("Update impossible : L'histoire n'est pas reconnue");
		}
	}
	
	// Suppression d'une histoire
	public void deleteHistoireById(int idHistoire) {
		if(histoireDao.findById(idHistoire) != null) {
			histoireDao.deleteById(idHistoire);
		} else {
			System.out.println("Suppression impossible : L'id de l'histoire n'est pas reconnu");
		}
	}
	
}
