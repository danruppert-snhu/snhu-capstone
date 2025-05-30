---
layout: default
title: Databases
nav_order: 2
---
<div style="text-align: center; white-space: nowrap; font-size: 16px; margin-bottom: 10px;">
  <a href="/snhu-capstone/">Home</a> |
  <a href="/snhu-capstone/code-review/">Code Review</a> |
  <a href="/snhu-capstone/enhancements/software-engineering/">Software Engineering</a> |
  <a href="/snhu-capstone/enhancements/data-structures-algorithms/">Algorithms</a> |
  <a href="/snhu-capstone/enhancements/databases/">Databases</a> |
  <a href="/snhu-capstone/artifacts/">Artifacts</a> |
  <a href="/snhu-capstone/self-assessment/">Self-Assessment</a>
</div>
<hr>

# Milestone Four: Enhancement Three – Databases

The artifact I selected for the databases category is the same Android application enhanced throughout this capstone project, initially developed in CS-360. I chose this artifact because its data management functionality provided rich opportunities to improve schema design, performance optimization, and security practices. As part of the enhancement, I significantly expanded the database schema to support recurring events, introduced foreign key constraints for referential integrity, created new indexes for performance, enhanced password hashing, and implemented transactional inserts for bulk operations. Additionally, I created a normalized recurrence type lookup table and added several new columns to support relational consistency and extensibility.

I selected this artifact for inclusion in my ePortfolio because it represents my ability to design a secure, scalable, and performant data model within the constraints of a mobile application. Enhancements that reflect my database skills include the use of parameterized queries throughout the application to prevent SQL injection, the use of indexing and lookup tables to improve query speed and relational integrity, and schema updates that enforce data consistency. Most importantly, the artifact directly demonstrates the development of a security mindset, meeting course outcome five, through the implementation of secure password storage using PBKDF2 hashing with salt, increased hash iterations for stronger resistance against brute force attacks, and the exclusion of any plaintext credential storage. These changes demonstrate a deliberate effort to anticipate and mitigate potential vulnerabilities in both storage and data access logic.

With this enhancement, I successfully met the course outcome I planned to address, Outcome 5, which focuses on developing a security mindset. By securing user credentials, normalizing data structures, and using secure coding practices for all database interactions, I’ve applied both foundational and advanced principles in secure software architecture. No changes were required to my outcome coverage plan, as the database enhancements aligned precisely with the original goal of demonstrating data security and robust design.

During the enhancement process, I significantly deepened my understanding of mobile database management and secure data handling practices. I learned how to construct and evolve a relational schema that could support expanding feature sets while preserving data integrity. This included hands-on experience with Android’s SQLite API, as well as applying transactional logic and parameterized queries to ensure consistency and protection against SQL injections. One of the key challenges I encountered was managing schema evolution, specifically, introducing foreign key constraints and new columns without compromising application stability. In the context of this project, resetting the events database was an acceptable trade-off, but I recognize that such an approach would not be suitable in a production environment where preserving user data is critical. Addressing this forced me to think more like a systems architect, weighing design trade-offs while balancing usability, performance, and security across the data layer of a real-world mobile solution.