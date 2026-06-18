package com.ensah.Core.schedulers;

import com.ensah.Core.dao.ITacheRepository;
import com.ensah.Core.model.Tache;
import com.ensah.Core.services.IEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ReminderScheduler {

    @Autowired
    private ITacheRepository tacheRepository;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private com.ensah.Core.dao.IAnnotationRepository annotationRepository;

    // Run every minute for testing. For production, change to: @Scheduled(cron = "0 0 8 * * ?")
    @Scheduled(cron = "0 * * * * ?")
    public void sendReminders() {
        System.out.println("====== CRON JOB DE RAPPEL DÉMARRÉ (" + java.time.LocalDateTime.now() + ") ======");
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Tache> tasksDueTomorrow = tacheRepository.findByDateLimite(tomorrow);
        
        System.out.println("Recherche des tâches avec date limite = " + tomorrow + " : " + tasksDueTomorrow.size() + " tâche(s) trouvée(s).");

        for (Tache task : tasksDueTomorrow) {
            long annotatedCount = annotationRepository.countByTacheIdAndAnnotateurId(task.getId(), task.getAnnotateur().getId());
            boolean terminee = annotatedCount >= task.getCouples().size() && task.getCouples().size() > 0;
            
            if (!terminee) {
            String email = task.getAnnotateur().getEmail();
            if (email != null && !email.isEmpty()) {
                String subject = "Rappel : Date limite d'annotation demain !";
                String text = "Bonjour " + task.getAnnotateur().getPrenom() + ",\n\n" +
                        "Ceci est un rappel concernant le dataset : " + task.getDataset().getNomDataset() + ".\n" +
                        "La date limite d'annotation est fixée à demain (" + tomorrow.toString() + ").\n" +
                        "Il vous reste encore des paires de textes à annoter.\n\n" +
                        "Merci pour votre contribution !\n" +
                        "L'équipe NLP Annotation Platform.";
                
                try {
                    emailService.sendReminderEmail(email, subject, text);
                    System.out.println("Email envoyé à : " + email);
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'envoi de l'email à " + email + " : " + e.getMessage());
                }
            }
        }
        }
    }
}
