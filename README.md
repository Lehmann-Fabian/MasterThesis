# Master's Thesis - Fabian Lehmann

## Experiments
#### Before you run the experiments: <br>
1. ```cd <project's root directory>```<br>
1. ```git clone https://github.com/rancher/local-path-provisioner.git```<br>
1. ```cd local-path-provisioner```<br>
The newest version had a bug; we use an older commit:<br>
1. ```git checkout c3192ec3f19f2bdb84a13a830d4160f44963c92c```<br>
1. ```cd ..```<br>
Furthermore, we need to clone our Kubespray's fork and MockFog2's fork into this project's root directory<br>
1. ```git clone https://github.com/Lehmann-Fabian/kubespray.git```<br>
1. ```git clone https://github.com/Lehmann-Fabian/MockFog2.git```<br>
1. ```cd MockFog2```<br>
Switch to our development branch:<br>
1. ```git checkout development```<br>

#### To change an experiment's parameters:<br>
Change parameters in `//<project's root>/orchestration/case<experiment ID>.csv` (experiments start with 0)<br>
Start Jupyter-Lab<br>
- open /notebooks/Generator
- Run all cells

#### Run the experiments:<br>
```bash run-all-experiments.sh```<br>

## Evaluation
To run the evaluation, you need to install dictdiffer:<br>
```pip install dictdiffer```<br>
Start Jupyter-Lab<br>
In Jupyter-Lab:<br>
- open /notebooks/Analytics
- In the third cell: set the experiment you want to evaluate 
- Run all cells
- Plots are written into `//<project's root>/evaluation/<experiment ID>/`


## Services
Producer Service in `//<project's root>/producer/`<br>
Filter and Cleaner Service in `//<project's root>/fitler/`<br>
Anomaly Detection Service in `//<project's root>/analyst/`<br>
Management Service  in `//<project's root>/manager/`<br>

#### Create and upload Docker-images
```build.bat```


