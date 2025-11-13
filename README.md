# 🧩 Grails Logical Delete Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.grails.plugins/grails-logical-delete.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.grails.plugins/grails-logical-delete)
[![Java CI](https://github.com/grails-plugins/grails-logical-delete/actions/workflows/gradle.yml/badge.svg?event=push)](https://github.com/grails-plugins/grails-logical-delete/actions/workflows/gradle.yml)

The **Grails Logical Delete** plugin provides a simple way to implement logical deletion (soft delete)
of domain class instances in Grails applications.

Instead of permanently removing records from the database, the plugin marks them as deleted,
allowing you to preserve data integrity, maintain audit history, and safely “undelete” entities when needed.

## ✨ Features

- **Transparent soft-deletes** — automatically updates a flag instead of issuing DELETE statements.
- **Query filtering** — excludes logically deleted records from queries by default.
- **Undelete support** — easily restore logically deleted records.

## 📚 Documentation

[Latest release](https://grails-plugins.github.io/grails-logical-delete/latest/) | [Latest snapshot](https://grails-plugins.github.io/grails-logical-delete/snapshot/)

