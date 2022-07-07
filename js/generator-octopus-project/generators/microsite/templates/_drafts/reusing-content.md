---
layout: page
title: Reusing content in multiple docs
order: 60
---

Sometimes the content that you create is needed in multiple docs. For instance, perhaps your document touches elements on both configuration features and variables and could be included with the docs about variables, the docs about configuration features, and in the deployment example docs. If you added the content to all three areas of the documentation, you would then need to keep all three copies up to date if anything changes.

With includes you can create the content once, and then include it in all the docs that need that content. This makes it easier for users to find relevant content where they need it. See [Using and Defining Includes](#using-and-defining-includes) for details on creating the includes content. Once the content has been created, it should be saved to the `docs/shared-content/` folder.

# Considerations for creating reusable content

Remember when you reuse content in this manner readers will come to it from different contexts. For this reason it's important to make sure the content fits with each context it will be used with. Some things that can help are to ensure the content works as a standalone piece, for instance, **How to Configure X**, give the section a title and include an introduction so readers understand what this section will cover even though they might be approaching it from different contexts. Lastly, review the content in each context yourself to ensure it fits in each instance.
