# Contribution guidelines
Want to help build onto this project? Great! Here are some rules for contribution:

### Contribution of ideas
Don't want to code/model/texture but just have cool ideas? Definitely welcome! You can raise an issue using the feature request template and we will take a look at it.

### Maintain feature parity
Adding a feature on one version means it needs to be added on all supported versions. You may request help porting it between mod loaders, but are responsible for porting it to the different versions for each mod loader.

### Branching and PRs
When adding a new feature, always work on a separate branch titled `feat/<name>`. If possible, relate the name to a specific section of the mod (e.g. for Act I, call it `feat/act_1_<featname>`).
Once your feature is implemented on ONE version of Minecraft (e.g. Neoforge 26.2), submit a pull request. We will review, evaluate and refine the feature on that singular version. Once confirmed, you will be required to maintain feature parity across versions as mentioned above before it is merged into the main release.
