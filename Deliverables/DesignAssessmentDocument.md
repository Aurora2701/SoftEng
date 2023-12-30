# Design assessment


```
<The goal of this document is to analyse the structure of your project, compare it with the design delivered
on April 30, discuss whether the design could be improved>
```

# Levelized structure map
```
<Applying Structure 101 to your project, version to be delivered on june 4, produce the Levelized structure map,
with all elements explosed, all dependencies, NO tangles; and report it here as a picture>
```

![image](./img/design%20assessment/LSM.PNG)

# Structural over complexity chart
```
<Applying Structure 101 to your project, version to be delivered on june 4, produce the structural over complexity chart; and report it here as a picture>
```

![image](./img/design%20assessment/soc.PNG)


# Size metrics

```
<Report here the metrics about the size of your project, collected using Structure 101>
```



| Metric                                    | Measure |
| ----------------------------------------- | ------- |
| Packages                                  |    6     |
| Classes (outer)                           |    42     |
| Classes (all)                             |    42     |
| NI (number of bytecode instructions)      |   7639      |
| LOC (non comment non blank lines of code) |   3285      |



# Items with XS

```
<Report here information about code tangles and fat packages>
```

| Item | Tangled | Fat  | Size | XS   |
| -------------- | ------- | ---- | ---- | ---- |
| ezshop.it.polito.ezshop.data.EZShop |         | 185 | 3940 | 1384 |
| ezshop.it.polito.ezshop | 14% | 4 | 7639 | 1056 |
| ezshop.it.polito.ezshop.data.EZShop.deleteProductFromSale(java.lang.Integer, java.lang.String, int):boolean     |         |   20  |   151   |  37 |
| ezshop.it.polito.ezshop.data.EZShop.addProductToSale(java.lang.Integer, java.lang.String, int):boolean     |         |    17  |  140    |    16  |
| ezshop.it.polito.ezshop.data.EZShop.modifyCustomer(java.lang.Integer, java.lang.String, java.lang.String):boolean     |         |   16   |   172   |    10  |




# Package level tangles

```
<Report screen captures of the package-level tangles by opening the items in the "composition perspective" 
(double click on the tangle from the Views->Complexity page)>
```
![image](./img/design%20assessment/packageTangles.PNG)

![image](./img/design%20assessment/tangles.PNG)

# Summary analysis
```
<Discuss here main differences of the current structure of your project vs the design delivered on April 30>
<Discuss if the current structure shows weaknesses that should be fixed>
```
The main differences in the current design with respect to the one delivered on April 30th are:

- Class AccountBook removed: as we proceeded in coding, we realized that it was not needed, since we already had other 
  classes and data structures that provided us the information and functionalities that were meant to be provided by the
  AccountBookClass
   

- Class Product removed: as for the AccountBook, Product was not needed because of redundancy - it was meant to keep 
  information about the quantity of items for each ProductType, but the API that we were provided with at the start of 
  the coding phase made it quite obvious that the quantity could just be an attribute in ProductType
  

- Class Order does not extend BalanceOperation: when following the requirements we decided to make it do so because it 
  made sense (just like making Sales and Returns inherit from BalanceOperation), but since the interfaces were in 
  conflict with each other we had to adopt another solution, therefore we removed the inheritance between these two
  classes.
  
The current structure shows some weaknesses highlighted in the previous paragraphs, regarding the presence of tangles 
and fat packages/classes/methods. However, most of this complexity was inevitable:

- EZShop is a fat class because it is there as a facade, through which the outer components (GUI) interact with other 
  classes in the package
  

- The tangles between 'data' and 'utils' packages are due to the interactions with the DB, that has to be kept 
  consistent through the execution of the application 