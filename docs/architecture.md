\# Architecture du projet



Le projet est développé selon le modèle MVC (Model View Controller).



\## Models



Contiennent les données de l'application.



\* ProjectEntry

\* ProjectStore



\## Views



Contiennent les interfaces graphiques JavaFX.



\* MainView

\* WelcomeView

\* OpenProjectView

\* NewProjectView



\## Controllers



Gèrent la logique métier.



\* AppController

\* MainController

\* WelcomeController

\* OpenProjectController

\* NewProjectController



\## Flux principal



Utilisateur → View → Controller → Model



