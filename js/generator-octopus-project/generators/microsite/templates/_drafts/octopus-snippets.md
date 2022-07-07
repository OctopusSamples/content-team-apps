---
layout: page
title: Octopus snippets
order: 50
permalink: octopus-snippets
family: snippets
---

This page lists the preferred naming conventions for Octopus specific terms and lists reusable snippets of text that can be included in the blog, the docs, or any other repo that has [added the snippets repo as a submodule](snippets-submodule.md).

To include specific snippet text, use the following syntax:

```
!include <snippet-name>
```

Or if they don't quite fit your purpose, copy and paste, and use them as a starting point.

## Taxonomy

The following types of snippets are available:

- **One line**: A short sentence that states what something is but doesn't provide any context. 
- **Brief**: A brief sentence or paragraph that states what something is and what problem it solves.
- **Intro**: An introduction to the concept that states what something is, what problem it solves, and how to start applying it.

## Available snippets

<ul>
{% for term in site.terms %}
 <li><a href="#{{ term.handle }}">{{ term.title }}</a></li>
{% endfor %}
</ul>

{% for term in site.terms %}
## {{ term.title }}
	{% if term.notes %}
{{ term.notes }}
	{% endif %}
	{% if term.one-line == true %}
		{% assign one-line = term.handle | append: "-one-line.include.md" | prepend: "snippets/" %}

**One line snippet**:

To include, use: 

`!include <{{ term.handle | append: "-one-line" }}>`

{% include {{ one-line }} %}
	{% endif %}
	{% if term.brief == true %}
		{% assign brief = term.handle | append: "-brief.include.md" | prepend: "snippets/" %}

**Brief snippet**:

To include, use: 

`!include <{{ term.handle | append: "-brief" }}>`

{% include {{ brief }} %}
	{% endif %}
	{% if term.intro == true %}
		{% assign intro = term.handle | append: "-intro.include.md" | prepend: "snippets/" %}

**Intro snippet**:

To include, use: 

`!include <{{ term.handle | append: "-intro" }}>`

{% include {{ intro }} %}
	{% endif %}
{% endfor %}
    
