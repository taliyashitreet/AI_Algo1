# AI ALGO
In this task I was required to load a Bayesian network I read from an xml file.
I had to answer an input file that included probabilistic queries while extracting an output file with the appropriate answers.

In order to do this I had to implement two different algorithms:
## 1. Bayes ball 
## 2. variable elimination


# Details of the task files:
## Ex1- where the main function is. Reading the files is done there, building the network, and answering the queries.
## BaseNode- An object I created which extends the Node and it describes the nodes of the Bayesian network in particular each Node contains a cpt table
## VariableElimination - Another object I created that is responsible for all the elimination of variables and returning the desired answer to a query
