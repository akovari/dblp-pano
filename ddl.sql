use DBLP;
create or replace stage dblp_stage file_format = (type = 'json');
put file://./data/tocs.json  @dblp_stage ;
put file://./data/publications.json  @dblp_stage ;
put file://./data/publications.json  @dblp_stage ;
create or replace table TOCS (key varchar not null, page_url varchar not null);
copy into TOCS (key, page_url) FROM (SELECT $1:key, $1:page_url FROM  @dblp_stage/tocs.json.gz t);

create or replace table PUBLICATIONS (key varchar not null, title varchar, year int, journal_title varchar, book_title varchar, toc_key varchar, mdate date, ids object);
copy into PUBLICATIONS (key, title, year, journal_title, book_title, toc_key, mdate, ids) FROM (SELECT $1:key, $1:title, $1:year, $1:journal_title, $1:book_title, $1:toc_id, $1:mdate, $1:ids FROM  @dblp_stage/publications.json.gz t) on_error=continue;

create or replace table PERSONS (key varchar not null, primary_name varchar not null, pid varchar not null, most_recent_mdate date, mdate date, number_of_publications int, ids object, number_of_coauthors int, publication_ids array);
copy into PERSONS (key, primary_name, pid, most_recent_mdate, mdate, number_of_publications, ids, number_of_coauthors, publication_ids) FROM (SELECT $1:key, $1:primary_name, $1:pid, $1:most_recent_mdate, $1:mdate, $1:number_of_publications, $1:ids, $1:number_of_coauthors, $1:publication_ids FROM  @dblp_stage/persons.json.gz t) on_error=continue;

create view publication_authors as (
  select pub.key as publication_key, per.key as person_key
  from dblp.public.publications as pub
  right join dblp.public.persons as per on (array_contains(pub.key::variant, per.publication_ids))
  limit 10
);
grant all privileges on schema public to public;
