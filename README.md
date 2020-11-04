# Master's Thesis - Fabian Lehmann

### Experiments
Before you run the experiments: 

```cd <project's root directory>```
```git clone https://github.com/rancher/local-path-provisioner.git```
```cd local-path-provisioner```
The newest version had a bug; we use an older commit:
```git checkout c3192ec3f19f2bdb84a13a830d4160f44963c92c```
```cd ..```
Furthermore, we need to clone our Kubespray's fork and MockFog2's fork into this project's root directory
```git clone https://github.com/Lehmann-Fabian/kubespray.git```
```git clone https://github.com/Lehmann-Fabian/MockFog2.git```
```cd MockFog2```
Switch to our development branch:
```git checkout development```

To change an experiment's parameters:
Chnage parameters in `//<project's root>/orchestration/case<experiment ID>.csv` (experiments start with 0)
Start Jupyter-Lab
- open /notebooks/Generator
- Run all cells

Run the experiments:
```bash run-all-experiments.sh```

### Evaluation
To run the evaluation, you need to install dictdiffer:
```pip install dictdiffer```
Start Jupyter-Lab
In Jupyter-Lab:
- open /notebooks/Analytics
- In the third cell: set the experiment you want to evaluate 
- Run all cells
- Plots are written into `//<project's root>/evaluation/<experiment ID>/`


## Services
Producer Service in `//<project's root>/producer/`
Filter and Cleaner Service in `//<project's root>/fitler/`
Anomaly Detection Service in `//<project's root>/analyst/`
Management Service  in `//<project's root>/manager/`

#### Create and upload Docker-images
```build.bat```


